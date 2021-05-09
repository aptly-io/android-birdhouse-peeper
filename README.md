# Birdhouse Peeper

An Android app to manage Birdhouse Peeper IoT sensors.

The provisioning uses the `com.github.espressif:esp-idf-provisioning-android` library.
The IoT uses the esp-idf example `provisioning/legacy/ble_prov`.

Setup these, and dont commit:

```kotlin
private const val SSID = "Your AP's SSID"
private const val PASSPHRASE = "Your AP's passphrase"
private const val PROOF_OF_POSSESSION = "Match the value in the Birdhouse Peeper IoT device"
```

Maaany TODO's:

- on a background thread
- refactor the call-back/listeners
- handle credentials properly
- change the device's prefix (e.g. "BHP")
- provision for running an AP (now only STA)
- proper UI/fragment
