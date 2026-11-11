# Sahtein (صحتين) — v0.1

Android diet & workout tracker. Free, offline and formula-based. It has no AI yet.

## What's in this build
- **Onboarding.** Collects sex (Male/Female, changeable later in Settings), birth year, height, weight, goal, activity, pace and meal times. From these it builds the plan: calories (Mifflin-St Jeor), macros, a safety floor and an expected goal date.
- **Meal reminders.** One reminder at each meal time, with *Snooze 15 min* / *Skipped* actions. If the meal still isn't logged after 60 minutes, you get one follow-up. A daily summary notification arrives at the time you choose.
- **Today screen.** Shows a calorie ring, macro bars, meal cards and a countdown to the next meal. Each meal gets a verdict (on track / over / under / low protein) with a concrete fix, such as "cut 1 loaf to save ~250 kcal".
- **Logging:**
  - Search ~90 Egyptian and everyday foods (Arabic/English/Franco).
  - **Voice or typed sentence** in Egyptian Arabic or English, e.g. `رغيفين فول وبيضة مسلوقة`. The phone's own speech recognition does the listening and a rule-based parser turns it into foods (no AI, no cost).
  - Re-log recent foods, copy yesterday's meal, or add a custom food.
- **Daily summary.** A shareable message with every meal, totals, macros, XP and streak.
- **History and weekly report.** A colour-coded calendar, day detail, and a weekly report (Saturday start) with adherence %, averages, best day, top foods, workouts and a comparison with last week.
- **Workouts.** Gym/Home switch and 14 muscles × 56 exercises. Each exercise has steps, mistakes to avoid, sets × reps and a **Watch on YouTube** button. There are 6 ready plans (the recommendation depends on Male/Female), an active workout logger with a rest timer and last-time numbers, and calories burned via MET.
- **Progress.** Levels and XP, logging and on-target streaks, a weight log with a chart, and 12 badges.
- **Settings.** Profile, Male/Female, activity, pace, meals and percentages, reminders on/off, summary time, whether workout calories are added back to the food budget, the exact-alarm permission, and delete all data.

Arabic or English follows the phone language (full RTL). The Arabic wording switches between masculine and feminine forms.

## Build
**Android Studio:** File → Open → this folder → wait for Gradle sync → Run ▶ on a phone or emulator.

**GitHub (no Android Studio):** push this folder to a GitHub repo. The workflow in `.github/workflows/build.yml` builds `app-debug.apk`, which you download from the run's *Artifacts*.

Stack: Kotlin 2.2, Jetpack Compose (Material 3), Room, Navigation-Compose, AGP 8.11, min SDK 26, target SDK 36.

## Project map
```
domain/   Lang (bilingual + gendered strings), FoodDb, Parser (voice/text), Calc (BMR/TDEE/verdicts), ExerciseDb + plans
data/     Room entities & DAO, Repo, Stats (XP, streaks, badges, weekly report)
notify/   AlarmManager reminders, follow-ups, daily summary, boot rescheduling
ui/       Compose screens: Onboarding, Today, Log, History/Week, Workouts, Progress, Settings
```

## Next (from the spec)
Firebase sync/backup, barcode scanner (ML Kit + Open Food Facts), a home-screen widget, the body-map picker, curated YouTube video IDs, and nutritionist review of the food DB. After that comes the AI update (photo logging, AI parsing).
