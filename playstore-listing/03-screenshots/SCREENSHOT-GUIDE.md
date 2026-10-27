# Screenshots: what to capture and how to turn them into store images

`output-phone/sample-*.png` shows the finished look, built from 3 draft captures (`drafts/`). Replace them with your own.

## Play Store rules
- Phone: **2–8 screenshots**, PNG or JPEG, 9:16, at least 1080 px on the short side. The template outputs **1080×1920**.
- You need **at least 4** screenshots at 1080 px or more to be eligible for featuring and for the large preview on the store.
- The first 3 do most of the work: on the store page most people see only those.
- Tablet screenshots (7" and 10") are optional. Skip them unless you want tablet featuring.

## 1. Capture (8 raw screenshots)

Save them into `03-screenshots/raw/` with exactly these names:

| # | File name | Screen | How to get there | Tip |
|---|---|---|---|---|
| 1 | `01-home-tonight-card.png` | Home with **Tonight's card revealed** | Play tab → tap the purple card | The strongest image, so it goes first |
| 2 | `02-vibe-picker.png` | "Who do you usually play with?" | Fresh install → onboarding page 4, tap one vibe | Pick **Friends** so the check mark shows |
| 3 | `03-in-game-card.png` | Live card during a game | Play a pack with 4 players → first card | Choose a **Deep** or **Intimate** card with a great question |
| 4 | `04-pack-heat-level.png` | Pack detail showing Heat Level | Open a pack with cards (e.g. *Conversation Starters*) | Make sure it doesn't show "0 CARDS" |
| 5 | `05-wrapped-awards.png` | DeepTalk Wrapped / awards | Finish a short game (1 round) | Use real-looking names: Emma, Liam, Sofia, Noah |
| 6 | `06-pass-the-phone.png` | "Pass the phone to …" overlay | Game setup → House rules → Pass the phone ON | |
| 7 | `07-pack-collections.png` | Home scrolled to pack covers | Scroll the home feed to "Beyond First Impressions" | Set `"crop": 165` for this one in captions.json |
| 8 | `08-create-pack.png` | Library → My packs, or Create Pack | Library tab → My packs → ＋ | Fill in a title and a cover image first |

**For a clean status bar** (full battery, 9:41, no notifications), turn on demo mode before capturing:
```bash
adb shell settings put global sysui_demo_allowed 1
adb shell am broadcast -a com.android.systemui.demo -e command enter
adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 0941
adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false
adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4
adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false
adb exec-out screencap -p > raw/01-home-tonight-card.png
```

## 2. Render the marketing frames
```powershell
cd playstore-listing\03-screenshots\template
.\render.ps1                  # captions.json → ..\output-phone\01-… 08-….png
```
- Headlines, subtitles and color themes live in `template/captions.json`. Themes: `violet`, `sunset`, `rose`, `sky`, `ember`, `gold`.
- To trim the status bar off one screenshot, add `"crop": 120` (pixels) to that entry.
- For localized screenshots, copy `captions.json` to `captions.vi.json`, translate the text, capture with the phone set to that language, and run `.\render.ps1 -Captions captions.vi.json -OutDir ..\output-vi`.
- Needs Google Chrome or Microsoft Edge (both are found automatically).

## Caption set (already in captions.json)
| # | Headline | Subtitle |
|---|---|---|
| 1 | Skip small talk. *Get closer.* | A new question to spark every night |
| 2 | Who are you *with tonight?* | Friends, dates, parties or new groups |
| 3 | Flip a card. *Say it out loud.* | Swipe right to answer, left to forfeit |
| 4 | Choose how *hot it gets.* | From Mild icebreakers to Blazing |
| 5 | See who *won the night.* | Awards, leaderboard & card of the night |
| 6 | 2 to 20 players. *Your house rules.* | Pass the phone, speed mode, penalties |
| 7 | Decks for *every mood.* | Icebreakers, deep talks & date nights |
| 8 | Make your *own deck.* | Write the questions only your crew would ask |

## Promo video (optional, but it raises installs)
A 15–30 s YouTube video (public or unlisted, no ads) goes in the listing's **Video** field. Screen-record: tap to reveal Tonight's card → pick a vibe → swipe 3 cards → Wrapped awards. Use `adb shell screenrecord /sdcard/demo.mp4`.
