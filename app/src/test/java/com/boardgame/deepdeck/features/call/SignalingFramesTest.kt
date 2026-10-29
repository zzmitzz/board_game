package com.boardgame.deepdeck.features.call

import com.boardgame.deepdeck.features.call.signaling.ClientFrame
import com.boardgame.deepdeck.features.call.signaling.ServerFrame
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The frames as the backend sends them (backend_v2/tests/unit/test_call_rooms.py). */
class SignalingFramesTest {
    // Same configuration as NetworkModule.provideJson().
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    private fun decode(text: String) = json.decodeFromString(ServerFrame.serializer(), text)
    private fun encode(frame: ClientFrame) = json.encodeToString(ClientFrame.serializer(), frame)

    @Test
    fun `joined carries peers, history and ice servers`() {
        val frame = decode(
            """
            {"type": "joined", "room": "K7PXQ2", "peer_id": "me", "resumed": false, "capacity": 2,
             "peers": [{"peer_id": "p1", "name": "Alice", "audio": true, "video": false, "connected": true}],
             "history": [{"type": "chat", "id": "m1", "from": "p1", "name": "Alice", "text": "hi",
                          "sent_at": "2026-10-28T10:00:00.000+00:00"}],
             "ice_servers": [{"urls": ["stun:stun.l.google.com:19302"]},
                             {"urls": ["turn:turn.example.com:3478"], "username": "1:d", "credential": "x"}]}
            """.trimIndent()
        ) as ServerFrame.Joined

        assertEquals("me", frame.peerId)
        assertEquals(false, frame.peers.single().video)
        assertEquals("hi", frame.history.single().text)
        assertNull(frame.iceServers[0].username)
        assertEquals("x", frame.iceServers[1].credential)
    }

    @Test
    fun `relayed frames keep the sender`() {
        val ice = decode("""{"type": "ice", "candidate": "candidate:1", "sdp_mid": "0", "sdp_mline_index": 0, "from": "p1"}""")
        assertEquals(ServerFrame.Ice("candidate:1", "0", 0, "p1"), ice)
        val left = decode("""{"type": "peer_left", "peer_id": "p1", "reason": "timeout"}""")
        assertEquals(ServerFrame.PeerLeft("p1", "timeout"), left)
        val error = decode("""{"type": "error", "error": "chat_too_long", "message": "Message is too long", "ref": "m2"}""")
        assertEquals("m2", (error as ServerFrame.Error).ref)
    }

    @Test(expected = SerializationException::class)
    fun `unknown frame types are rejected so the client can skip them`() {
        decode("""{"type": "hologram", "from": "p1"}""")
    }

    @Test
    fun `client frames are tagged with their type`() {
        assertEquals("""{"type":"ping"}""", encode(ClientFrame.Ping))
        assertEquals("""{"type":"chat","id":"m1","text":"hi"}""", encode(ClientFrame.Chat("m1", "hi")))
        // explicitNulls = false: a missing sdp_mid is left out, not sent as null.
        assertEquals(
            """{"type":"ice","candidate":"c","sdp_mline_index":0}""",
            encode(ClientFrame.Ice("c", null, 0))
        )
    }

    @Test
    fun `room codes come out of pasted invites`() {
        assertEquals("K7PXQ2", CallStartVM.extractCode("Join me! Room code: K7PXQ2\ndeepdeck://call/K7PXQ2"))
        assertEquals("K7PXQ2", CallStartVM.extractCode("deepdeck://call/k7pxq2"))
        assertEquals("K7PXQ2", CallStartVM.extractCode("  k7pxq2 "))
        assertTrue(CallStartVM.extractCode("no code in this message at all") == null)
    }
}
