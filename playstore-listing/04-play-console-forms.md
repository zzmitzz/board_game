# Play Console forms: ready-to-paste answers

These are the forms under **Policy and programs → App content**. The answers come from the code as it is today: `com.boardgame.deepdeck`, v1.0, the merged release manifest, and the API calls in `data/remote/`.

---

## 1. Privacy policy
- **URL:** ⚠️ the current links in the app return **404** (`uncertain-amethyst-pvpmhpc5.edgeone.dev`, `inclined-scarlet-jn0reqb4.edgeone.dev`). Host `04-privacy-policy.html` (in this folder) somewhere permanent, for example GitHub Pages, Firebase Hosting or Notion public. Then paste that URL here **and** update `PRIVACY_URL` / `TERMS_URL` in `features/you/YouScreen.kt`.
- The policy must name the same app as the store listing: **DeepTalk: Icebreaker Card Game** ✅ (it already does).
- Add **Google Analytics for Firebase** to the third-party list in the policy. Right now it only lists Google Play Services.

## 2. App access
**All functionality is available without special access.** There is no login and no paywall. Premium packs are shown as "coming soon" and can't be bought.

## 3. Ads
**No, my app does not contain ads.** There is no ad SDK in `build.gradle.kts`.

## 4. Advertising ID
The merged manifest contains `com.google.android.gms.permission.AD_ID` (added by Firebase Analytics).
- Answer **Yes, my app uses advertising ID**, with purpose **Analytics**.
- *Or* remove it and answer **No** by adding this to `AndroidManifest.xml`:
  ```xml
  <uses-permission android:name="com.google.android.gms.permission.AD_ID" tools:node="remove"/>
  ```

## 5. Content rating (IARC questionnaire)
| Question area | Answer | Reason |
|---|---|---|
| Category | **Game** (not casino) | |
| Violence / blood / fear | No | Text-only question cards |
| Sexuality | **Yes: sexual references / innuendo in text, no nudity** | "After Dark", "Touch & Tension", "Raw Desires", "Sensual Horizons" packs (heat 60–100) and the NSFW toggle |
| Language | **Mild or crude humour possible** | Party question decks |
| Controlled substances | **Yes: references to alcohol, and the game encourages drinking** | "Drinking" vibe, default forfeit "Take a sip" |
| Gambling / simulated gambling | No | |
| User interaction: can users chat or share content with other users? | **No** | Play is local (pass the phone). Sharing is a plain image through the Android share sheet. |
| Shares the user's location | No | |
| Digital purchases | No (answer Yes once Premium ships) | |

**Expected result:** roughly PEGI 16–18, ESRB Mature 17+, USK 16–18. Encouraging alcohol use usually pushes the rating to 18 in some regions. Answer honestly: a rating that doesn't match the content gets the app removed.

## 6. Target audience and content
- **Target age groups:** **18 and over** only.
  *Why:* the privacy policy says under-16s aren't the audience, and the app has drinking and sexual content. Picking any group under 13 pulls you into the Families policy (COPPA, certified ad SDKs, and more).
- **Could the app unintentionally appeal to children?** **No**, because the art, copy and content are adult-oriented.
- **Store listing appeals to children?** No.

## 7. Data safety

**Overview**
| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **Yes** |
| Is all of the user data collected by your app encrypted in transit? | ⚠️ **No, until the API moves to HTTPS.** The app calls `http://103.78.3.184:8000` over cleartext. Once HTTPS is in place, answer **Yes**. |
| Do you provide a way for users to request that their data is deleted? | **Yes**, by emailing facebookco@duck.com, as the policy says. |
| Account creation | **My app does not allow users to create an account** |

**Data types collected**

| Data type | Collected | Shared | Optional? | Purposes | Where in the code |
|---|---|---|---|---|---|
| **Device or other IDs** | Yes | No | Required | Analytics, App functionality | `DeviceIdRepository` (random install UUID sent with `/events/sessions` and `/cards/feedback`), Firebase app-instance ID |
| **App activity → App interactions** | Yes | No | Required | Analytics | Firebase Analytics events; `GameSessionCreate` (pack, player count, rounds, cards done or forfeited, duration, locale); card reactions and reports |
| **App info & performance → Diagnostics** | No | No | | | No Crashlytics yet (answer Yes if you add it) |
| Personal info (name, email, …) | **No** | | | | Player names stay on the device (`RosterRepository`) and are never uploaded |
| Photos | **No** | | | | Cover and card images for custom packs are stored locally only (Room) |
| Location | **No** (see note) | | | | |

- *"Shared"* = sent to a third party for its own use. Firebase acts as a service provider, so under Play's definition it counts as collection, not sharing.
- *Location:* Firebase derives a coarse location from the IP address. Before submitting, compare these answers with Google's official list for Firebase: https://firebase.google.com/docs/android/play-data-disclosure
- Card text sent to `/cards/translate` is app content, not user data.

## 8. Other declarations
| Form | Answer |
|---|---|
| News app | No |
| COVID-19 contact tracing / status | No |
| Government app | No |
| Financial features | My app doesn't provide any financial features |
| Health apps | My app does not have any health features |
| Photo & video permissions | Not applicable: the app uses the system document picker (`OpenDocument`), not `READ_MEDIA_*` |
| Foreground service | Only the WorkManager library's default declaration. If Play asks, the purpose is "Data sync" (queued session upload); there is no user-visible foreground service. |
| Exact alarms | Not used (WorkManager) |
