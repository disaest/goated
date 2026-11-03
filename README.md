# Goated

Каталог видеоигр с оценками и рецензиями (Kotlin, Jetpack Compose, MVVM, Supabase).

## Запуск

1. Откройте папку `Goated_final` в Android Studio.
2. Gradle JDK — 17 или 21 (Settings → Build Tools → Gradle → Gradle JDK).
   JDK 25, который идёт с новыми версиями Android Studio, Gradle 8.13 не поддерживает.
3. В `local.properties` должны быть указаны:
   ```
   sdk.dir=<путь к Android SDK>
   supabase.url=https://qgljrfbpscryhdfbqfyh.supabase.co
   supabase.key=<publishable key>
   ```
   Без `supabase.*` используются значения учебного проекта по умолчанию.
4. Один раз выполните `supabase/goated_fix_auth_reviews.sql` в Supabase → SQL Editor.
   Без него регистрация, оценки, рецензии и избранное работать не будут
   (на `profiles` включён RLS без политик SELECT/INSERT).

## Структура

```
com.example.goated/
├── MainActivity.kt
├── data/          — Models, SupabaseClient, AuthRepository, GameRepository, UserRepository, Errors
├── viewmodel/     — ViewModel всех экранов (состояния загрузки, ошибки, данных)
├── navigation/    — маршруты, 4 раздела нижней навигации
├── components/    — GameCard, StarRating, TopBar, BottomBar, ProfileTopBar, StateViews…
├── screens/       — Main, Search, Genres, CoopGenre, GameCard, Profile, Games, Registration
└── ui/theme/      — цвета, шрифт Geologica, размеры
```
