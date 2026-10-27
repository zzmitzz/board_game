# Pre-launch checklist: DeepTalk v1.0

Found while reviewing the code, the production API and the release build on 2026-09-28.
Fix the 🔴 items before you submit. Each one either gets the app rejected or brings in 1-star reviews on day one.

## 🔴 Blockers

### 1. 18 of 20 packs on the production server are empty
`GET /api/v1/packs/` returns 20 packs, but only **Conversation Starters (50 cards)** and **A Little Closer, Unspoken Stories (35 cards)** have any cards. The other 18 have `total_cards: 0`. I opened "First Footprints" in the app: it shows "0 CARDS" and "No sample cards yet".
- Play reviewers reject apps with broken or placeholder content under the Minimum Functionality policy. Users who open an empty pack will leave a bad review.
- The onboarding also promises "Hundreds of Themed Packs" (`onboarding_title_2`). Fill the decks first, or change that line to something true.
- **Fix:** load the cards into the backend, or hide packs where `total_cards == 0` (server-side or in the home feed).

### 2. The release build is signed with the DEBUG key
`app/build.gradle.kts` → `release { signingConfig = signingConfigs.getByName("debug") }`.
Play Console rejects debug-signed bundles. Create an upload key and keep it safe:
```
keytool -genkeypair -v -keystore deeptalk-upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
```
Add a `signingConfigs { create("release") { … } }` block that reads the passwords from `.env` or `local.properties` (never commit them). Then build with `./gradlew bundleRelease` and enrol in **Play App Signing** on the first upload.

### 3. Privacy policy and Terms links are dead (404)
`features/you/YouScreen.kt` points at two `edgeone.dev` URLs that both return 404. A working privacy policy URL is required on the listing *and* inside the app.
- Host `playstore-listing/04-privacy-policy.html` on something permanent, then update `PRIVACY_URL` / `TERMS_URL`.

### 4. The API runs over plain HTTP to a raw IP
`BASE_URL = "http://103.78.3.184:8000/"` with `usesCleartextTraffic="true"`.
- On the Data safety form you'd have to answer **"not encrypted in transit"**, which shows as a warning on the listing.
- The `X-API-Key` travels in clear text, so anyone on public Wi-Fi can read it.
- **Fix:** put the API behind a domain with TLS (for example Cloudflare, or Caddy/Nginx with Let's Encrypt), switch to `https://`, and remove the cleartext exceptions.

## 🟠 Should fix before launch

5. **Only English and Vietnamese are fully translated.** `values/` and `values-vi/` have 317 strings each; the other 11 languages have 42. Users in those languages will see a mostly English interface. The store listings were written to say "cards are translated into 13 languages", not "app in 13 languages". Before publishing the ja/ko/de/… listings, translate the app strings, or publish only the en-US and vi-VN listings for now.
6. **Adding players failed on the emulator (unconfirmed).** On a Pixel 8a emulator (API 36, release build), typing a name in Game setup and tapping the add button (or pressing Enter) cleared the field, but the roster stayed at 0/20, so a game couldn't start. The code looks correct, so this may be a quirk of adb text input. **Try it by hand on a real phone** before release: if it's real, nobody can play.
7. **"Premium – coming soon" packs.** Showing items you can't buy yet is fine, but keep "Premium" out of the store text until billing ships. The listing text in this folder already does.
8. **Pack detail layout:** the keyword chip overlaps the cover's "3+" badge when the keyword list wraps onto two lines (seen on "First Footprints").

## 🟡 Nice to have

9. `isMinifyEnabled = false`: the APK is 23.8 MB. Turning on R8 plus `isShrinkResources = true` usually cuts 40–60%, and a smaller download converts better on the store.
10. Add **Firebase Crashlytics** so you can see crashes from the first installs (then answer "Diagnostics: Yes" on Data safety).
11. The `API_KEY` is compiled into `BuildConfig`, so anyone who decompiles the app can read it. Rate-limit per device ID on the server.
12. Bump `versionCode` on every upload (Play rejects duplicates). Consider `versionName = "1.0.0"`.

## ✅ Already good
- `targetSdk 37`, which meets Play's target-API requirement.
- No login required, so there's nothing to set up for reviewers under App access.
- The notification permission is requested at runtime and is optional.
- Images are chosen through the system picker, so no sensitive storage permissions.
- Share uses a `FileProvider`, not `file://` URIs.

## Release flow (first time)
1. Play Console → **Create app** → name "DeepTalk: Icebreaker Card Game", Game, Free, then accept the declarations.
2. Fill in **App content** using `04-play-console-forms.md`.
3. Fill in **Main store listing** using `01-store-listing/` and `02-graphics/`, plus your screenshots in `03-screenshots/output-phone/`.
4. Add translations: Store listing → **Manage translations** → paste from `05-localization/`.
5. **Testing → Internal testing** → upload the `.aab` → add your testers' emails, then install and check the full flow.
6. **New personal developer accounts** (created after Nov 2023) must run a **closed test with at least 12 testers for 14 consecutive days** before they can apply for production access. Plan for it.
7. **Production** → roll out to 20% first, watch Android vitals and reviews, then go to 100%.
