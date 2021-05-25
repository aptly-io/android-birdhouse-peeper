package xyz.techmush.birdhouse_peeper.util

import android.content.Context
import android.content.pm.PackageManager


// extract permissions from the manifest through the package manager
// idea https://stackoverflow.com/questions/18236801/programmatically-retrieve-permissions-from-manifest-xml-in-android
fun extractPermissions(context: Context): Array<String> {
    with(context) {
        return packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS).requestedPermissions
    }
}

// ask for permission and return with those requiring explicit user consent
fun missingPermissions(context: Context, requiredPermissions: Array<String>) =
    requiredPermissions.filter { PackageManager.PERMISSION_GRANTED != context.checkSelfPermission(it) }
        .toTypedArray()
