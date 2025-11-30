# BlueTrigger (Android Bluetooth Automation)

BlueTrigger is an Android app that reacts instantly when a Bluetooth device connects or disconnects.  
You can assign custom actions like Lock Phone, Toggle Flashlight, Launch Camera, or Open an App.

It runs reliably in the background using a Foreground Service and BroadcastReceivers, even on modern Android versions (12 to 15).

---

## 🚀 Features
- Detects Bluetooth connect and disconnect events
- Runs custom actions instantly (lock device, flashlight, camera, apps)
- Works even when app is minimized or screen is locked
- Self-restarts using an internal watchdog
- Device-specific action mapping (Connect Action + Disconnect Action)
- Built entirely in Kotlin with Material Design 3 UI

---

## 📱 How to Run This Project on Your Device

### 1. Clone the Repository
git clone : https://github.com/drb101005/Blue-Trigger


### 2. Open in Android Studio
- Open **Android Studio**  
- Click **Open Project**  
- Select the **BluetoothActionTrigger** folder

### 3. Enable Developer Mode on Your Phone
1. Go to **Settings → About Phone**  
2. Tap **Build Number** 7 times  
3. Go to **Developer Options**  
4. Enable **USB Debugging**

### 4. Connect Your Phone
- Connect via USB  
- Approve the **"Allow USB debugging?"** popup  
- Run `adb devices` (optional)  
  It should show your device as **device**

### 4.5 (Optional) Verify ADB Connection

If you want to confirm ADB is working correctly, run:

adb devices

You should see your device listed as **device** (not unauthorized or offline).

---

### 4.6 (Optional) Improve Background Reliability Using ADB

These commands are optional but recommended.  
They make sure Android does not aggressively kill the service.

#### 1. Whitelist the app from Doze (battery optimization)
adb shell dumpsys deviceidle whitelist +com.example.bluetoothactiontrigger

#### 2. Mark the app as active in the system (better scheduling priority)
adb shell am set-inactive com.example.bluetoothactiontrigger false


#### 3. Simulate unplug (helps stabilize background scheduling)
adb shell cmd battery unplug


These do NOT require root and are completely safe.  
Your app will continue running more reliably in the background after this.


### 5. Build and Run
- In Android Studio, press **Run ▶**  
- Select your phone  
- App will install and open automatically

---

## 🔧 Required Permissions
The app will request these automatically:
- Bluetooth permissions  
- Camera (for flashlight toggle)  
- Device Admin (for Lock Phone)

Enable them when prompted.

---

## 🎮 How to Use Inside the App
1. Open BlueTrigger  
2. Select a paired Bluetooth device  
3. Choose:
   - **Connect Action**
   - **Disconnect Action**
4. Tap **Save Actions**  
5. Tap **Start Service** to activate background automation

The app will now:
- Trigger your action when the device connects  
- Trigger another action when it disconnects  

Even if the phone is locked.

---

## 🛠 Tech Stack
- Kotlin  
- Foreground Service  
- BroadcastReceivers  
- Bluetooth APIs  
- AlarmManager watchdog  
- SharedPreferences + Gson  
- Material Design 3

---

## 📸 Demo
- UI screenshot  
- Auto-lock demo video  
- Flashlight automation video

---

## 📌 Notes
- Works on Android 12 to 15  
- Designed to survive background restrictions  
- Fully offline, no data collection

---



