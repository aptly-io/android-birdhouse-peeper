package xyz.techmush.birdhouse_peeper.model

import android.content.Context
import androidx.core.content.edit
import timber.log.Timber

class BirdhouseRepository(private val context: Context) {

    data class Birdhouse(
        val address: String,
        val uuid: String,
        val ip: String,
        val port: Int,
        val name: String,
        val location: String,
        val photo: String,
        val ssid: String,
        val ssidPassword: String,
        val pop: String)

    private val birdhouseMap = mutableMapOf<String, Birdhouse>()

    fun read(): List<Birdhouse> {
        Timber.d("read")
        val addresses = context.getSharedPreferences("PREF_BIRDHOUSE_ADDRESSES", Context.MODE_PRIVATE)
            .getStringSet("ADDRESSES", setOf<String>()) ?: setOf()

        val birdhouses = addresses
            .filter { address -> address.isNotEmpty() }
            .map { address ->
                val birdhousePref =
                    context.getSharedPreferences("PREF_BIRDHOUSE_$address", Context.MODE_PRIVATE)
                val birdhouse = Birdhouse(
                    address,
                    birdhousePref.getString("UUID", "")!!,
                    birdhousePref.getString("IP", "192.168.4.1")!!,
                    birdhousePref.getInt("PORT", 80),
                    birdhousePref.getString("NAME", "BHP_$address")!!,
                    birdhousePref.getString("LOCATION", "")!!,
                    birdhousePref.getString("PHOTO_PATH", "")!!,
                    birdhousePref.getString("SSID", "BHP_$address")!!,
                    birdhousePref.getString("SSID_PASSWD", "")!!,
                    birdhousePref.getString("POP", "")!!)
                birdhouseMap[address] = birdhouse
                birdhouse
            }
        return birdhouses
    }

    fun read(address: String): Birdhouse? {
        Timber.d("read($address)")

        if (birdhouseMap.isEmpty()) {
            read()
        }
        return birdhouseMap[address]
    }

    fun save(birdhouse: Birdhouse) {
        Timber.d("save $birdhouse")

        val addressPrefs = context.getSharedPreferences("PREF_BIRDHOUSE_ADDRESSES", Context.MODE_PRIVATE)
        val addresses = addressPrefs.getStringSet("ADDRESSES", setOf<String>())?.toMutableSet() ?: mutableSetOf()
        addresses.add(birdhouse.address)
        addressPrefs.edit(commit = true) {
            putStringSet("ADDRESSES", addresses)
            putString(birdhouse.address, birdhouse.name)
        }

        context.getSharedPreferences("PREF_BIRDHOUSE_${birdhouse.address}", Context.MODE_PRIVATE).edit().let { prefs ->
            prefs.putString("ADDRESS", birdhouse.address)
            prefs.putString("UUID", birdhouse.uuid)
            prefs.putString("IP", birdhouse.ip)
            prefs.putInt("PORT", birdhouse.port)
            prefs.putString("LOCATION", birdhouse.location)
            prefs.putString("PHOTO_PATH", birdhouse.photo)
            prefs.putString("SSID", birdhouse.ssid)
            prefs.putString("SSID_PASSWD", birdhouse.ssidPassword)
            prefs.putString("POP", birdhouse.pop)
            prefs.apply()
        }
    }

    fun delete(birdhouse: Birdhouse) {
        Timber.d("delete $birdhouse")

        val addressPrefs = context.getSharedPreferences("PREF_BIRDHOUSE_ADDRESSES", Context.MODE_PRIVATE)
        val addresses = addressPrefs.getStringSet("ADDRESSES", setOf<String>())?.toMutableSet() ?: mutableSetOf()
        addresses.remove(birdhouse.address)
        addressPrefs.edit(commit = true) {
            putStringSet("ADDRESSES", addresses)
            remove(birdhouse.address)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.deleteSharedPreferences("PREF_BIRDHOUSE_${birdhouse.address}")
        } else {
            // I wanted to support from Build.VERSION_CODES.M (23) onwards ...
        }
    }
}