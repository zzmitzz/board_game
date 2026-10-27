# DeepTalk: Google Play publishing kit

Everything for the Play Store listing of **DeepTalk** (`com.boardgame.deepdeck`, v1.0).
Open **`store-preview.html`** in a browser to see the listing roughly as it will look on Google Play.

> ⚠️ Read **`06-release/pre-launch-checklist.md`** first. 4 blockers must be fixed before you submit: empty packs, debug signing, dead privacy URL, and the API on HTTP.

## What goes where in Play Console

| Play Console field | File |
|---|---|
| App name (30) | `01-store-listing/listing-en-US.md` → **DeepTalk: Icebreaker Card Game** |
| Short description (80) | `01-store-listing/listing-en-US.md` |
| Full description (4000) | `01-store-listing/listing-en-US.md` |
| App icon 512×512 | `02-graphics/app-icon-512.png` |
| Feature graphic 1024×500 | `02-graphics/feature-graphic-1024x500.png` |
| Phone screenshots (2–8) | `03-screenshots/output-phone/` (see `SCREENSHOT-GUIDE.md`) |
| Category, tags, contact email | `01-store-listing/name-keywords-and-category.md` |
| Privacy policy URL | host `04-privacy-policy.html` and paste its URL |
| App content (ads, rating, audience, data safety …) | `04-play-console-forms.md` |
| Translations (12 languages) | `05-localization/*.md` |
| Release notes | `01-store-listing/listing-en-US.md` (bottom) |

## Folder map

```
playstore-listing/
├─ store-preview.html              ← visual preview of the whole listing
├─ 01-store-listing/
│  ├─ listing-en-US.md             title · short · full description · release notes (counts verified)
│  └─ name-keywords-and-category.md  name options, category, tags, ASO keywords
├─ 02-graphics/
│  ├─ app-icon-512.png             Play icon (512×512, 32-bit PNG)
│  ├─ feature-graphic-1024x500.png
│  └─ source/                      editable HTML + fonts (Fraunces, Plus Jakarta Sans)
├─ 03-screenshots/
│  ├─ SCREENSHOT-GUIDE.md          the 8 shots to capture + captions
│  ├─ raw/                         ← drop your raw screenshots here
│  ├─ drafts/                      3 early captures from the emulator
│  ├─ output-phone/                framed 1080×1920 images (3 samples now)
│  └─ template/                    frame.html · captions.json · render.ps1
├─ 04-play-console-forms.md        App access, Ads, Advertising ID, Content rating, Target audience, Data safety …
├─ 04-privacy-policy.html          your policy, with Google Analytics for Firebase added
├─ 05-localization/                vi-VN · es-ES · pt-BR · fr-FR · it-IT · de-DE · ru-RU · tr-TR · id · hi-IN · ja-JP · ko-KR
└─ 06-release/
   └─ pre-launch-checklist.md      blockers, fixes, first-release flow
```

## Brand quick reference
| Token | Value |
|---|---|
| Background | `#0B0612` |
| Brand gradient | `#B57BFF → #8B3DFF` |
| Accents | Rose `#FF5C8A` · Sky `#6FC3FF` · Gold `#FFC76B` · Ember `#FF7A59` · Mint `#5EE6B0` |
| Headline font | Fraunces SemiBold (serif) |
| Body font | Plus Jakarta Sans |
| Tagline | *Skip small talk. Get closer.* |

## Editing graphics
- **Feature graphic:** edit `02-graphics/source/feature-graphic.html`, then re-render:
  ```powershell
  & "$env:ProgramFiles\Google\Chrome\Application\chrome.exe" --headless=new --hide-scrollbars --force-device-scale-factor=1 --window-size=1024,500 --virtual-time-budget=3000 --screenshot="$PWD\02-graphics\feature-graphic-1024x500.png" "file:///$($PWD -replace '\\','/')/02-graphics/source/feature-graphic.html"
  ```
- **Screenshots:** `03-screenshots/template/render.ps1` (see the guide).
