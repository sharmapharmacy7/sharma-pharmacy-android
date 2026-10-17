# Sharma Pharmacy Modinagar Android App (starter)

## Is project mein kya hai
- Medicine search
- Admin button se CSV stock file import (phone par local)
- Prescription photo choose karna
- WhatsApp par order text / prescription share karna
- Home-delivery contact notice
- GitHub Actions se online debug APK build

## CSV format
CSV ki pehli row headers ho sakti hai:
`Medicine Name,Stock,Price`
`Paracetamol 500,20,25.00`
`ORS Sachet,15,22.00`

Excel mein Save As -> CSV UTF-8 karke file phone/PC par rakhein.

## Online APK banane ke steps
1. GitHub par New repository banayein, naam `sharma-pharmacy-android` (Public ya Private).
2. Is ZIP ko extract karke iske andar ke sab files/folders repository mein upload karein. `.github` folder bhi upload hona chahiye.
3. Commit changes to `main`.
4. GitHub repository mein Actions tab kholein. `Build Sharma Pharmacy APK` workflow select karke `Run workflow` click karein. Push par bhi build automatically hota hai.
5. Build successful hone par run ke neeche Artifacts mein `Sharma-Pharmacy-debug-APK` download karein, ZIP extract karein, `app-debug.apk` phone mein bhejein aur install karein.

## Zaroori
- `app/src/main/java/in/sharmapharmacy/app/MainActivity.kt` mein `whatsappNumber` ko confirm karein. Country code ke saath digits, `+` ke bina.
- Yeh starter app CSV ko sirf current device par load karti hai. Roz stock sab customers/admin devices par automatically update karne ke liye online backend/database ya hosted CSV URL add karna hoga.
- Debug APK testing ke liye hai; Play Store/release distribution ke liye signed release build, privacy policy, legal/pharmacist checks aur testing chahiye.
- App order request bhejti hai, prescription-only medicines ko pharmacist ke verification ke baad hi supply karein.
