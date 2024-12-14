[//]: # (TODO remove or update this file)

# Build system
Expect the standard Android system we have the script `downloadLibwallet` which downloads ffi side from github release page to local. It will appear in `libwallet` folder. It downloads `wallet.h` and `libminotari_wallet_ffi.a` for each architecture.
If you want to test some local build you should turn off this download and put files manually

versionNumber is a version of our app. It should be aligned with iOS version
buildNumber is the build version, it should be incremented manually before the release
libwalletVersion is ffi version. It should be updated after ffi update and be released on tari github page





# FFI integration
FFI is written in Rust and built for Android for different architectures. We supported only arm64-v8a and x86_64 which are actual devices. Emulator has a different and we stopped maintaining it during the year 2023.
FFI lib files should be placed in the right folder with the right names. Now it's
libwallet/wallet.h
libwallet/arm64-v8a/libminotari_wallet_ffi.a
libwallet/x86_64/libminotari_wallet_ffi.a
Cmake files which build it very strict about it
Also for the right functioning, it should contain
* `libcrypto.a` - ssl
* libsqlite3.a - sqlLite
* libssl.a - ssl
If you need to update it then ssl is located here:
https://github.com/217heidai/openssl_for_android/releases





# FFI Wrapper
We have 2 wrappers, in C++ and kotlin
For C++ you should go to `\app\src\main\cpp`
It has CMake file which is responsible for C++ build. If you add a new cpp file you should include it to the build list in this file
C++ files contain code for method injection from native code which is described in `wallet.h`
You should name methods like this
`Java_com_tari_android_wallet_ffi_FFIContacts_jniGetLength`
where `Java_com_tari_android_wallet_ffi` is path, `FFIContacts` is kotlin wrapper file, and `jniGetLength` expected method.
Inside methods you should use methods from `wallet.h`
jniCommon.cpp contains utils method for enlight work with C++

kotlin wrappers have `FFI` prefix. They should only contain logic related to wrapping entities and methods
`private external fun jniGetBalance(libError: FFIError): FFIPointer`
This is an example of an external method which connected to the method in C++ wrapper
All FFI entities have a pointer to C++ entity which is located in FFIBase



# Wallet service





# Yat integration
https://yat.fyi/ - sandbox
https://y.at/ - prod
https://github.com/yat-labs/yat-lib-android - yat lib
https://jitpack.io/#yat-labs/yat-lib-android - yat build
But the last build was included manually because Jitpack won't build for Android 14. Needed to fix
In a project for all interactions with Yat responsible `YatAdapter`
We have those features:
* Connecting existing Yat
* Showing connected Yat in wallet frame
* Searching wallet address by Yat via Yat lib 




# BLE
Common:
Checking permissions for bluetooth_ and locations
Checking wheither bluetooth is turned on

Then server:
* Searching for device with our service inside
* Needed RSSI < -55db, which is about 20cm
* Pair to this device
* Discovering our service in BLE protocol
* Searching for exact characteristics in that service. We have two for sharing and getting contact in BLE payment
* Then read or write by small chunks

Then client:
* Creating service + both characteristics + callbacks
* Laucnching advetiser
* Waiting for connection

For working with BLE we are using 
https://github.com/weliem/blessed-android
It close to native but easier to use





# Release policy




# Github policy
