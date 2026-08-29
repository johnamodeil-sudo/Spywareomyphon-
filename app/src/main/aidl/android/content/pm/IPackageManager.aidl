package android.content.pm;
interface IPackageManager {
    void deletePackageAsUser(String packageName, int versionCode, in IPackageDeleteObserver2 observer, int flags, int userId);
}
