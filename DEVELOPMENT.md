# Developing Unlauncher

## Building a release

Building an Unlauncher release is straightforward.  

1. Push a tag to GitHub (e.g. `1.2.1`) from the latest commit on the `master` branch
    1. Make sure that the `versionName` in the [build.gradle.kts](./app/build.gradle.kts) matches the tag that you are pushing
1. Add release notes to the draft Release on GitHub that was created by the CI and publish the release
1. Prepare for the next release by incrementing the `versionCode` and `versionName` in the [build.gradle.kts](./app/build.gradle.kts) file
1. Monitor the [F-Droid build status](#checking-f-droid-build-status) to make sure the tag is successfully published (can take several days depending on the build queue)

## Building a beta release

> A beta release should be published ahead of an official release any time there are major changes to the app or the build process.

Unfortunately, F-Droid does not have an automatic process for triggering "beta" releases like it does for normal automatic updates [yet](https://gitlab.com/fdroid/fdroidserver/-/issues/161).
However, an F-Droid user will only be prompted to upgrade an app (or have the app be auto-upgraded) if their locally installed app has a version/code that is less than the `CurrenVersion`/`CurrentVersionCode` defined in the [fdroiddata yml config file](https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/com.jkuester.unlauncher.yml).

So, creating a beta release for Unlauncher requires the following steps:

1. Push a beta tag to GitHub (e.g. `2.0.0-beta.1`)
   1. Make sure that the `versionName` in the [build.gradle.kts](./app/build.gradle.kts) matches the tag that you are pushing
1. Delete the draft Release on GitHub for the beta tag that was created by the CI
1. Raise a MR to [fdroid/fdroiddata](https://gitlab.com/fdroid/fdroiddata) to add a new `Builds` entry for the beta release
    1. _Do not_ update the configured `CurrenVersion`/`CurrentVersionCode` since that will trigger a normal release
1. Prepare for the next release by incrementing the `versionCode` and `versionName` in the [build.gradle.kts](./app/build.gradle.kts) file

## Checking F-Droid build status

The status for the latest Unlauncher F-Droid build can be found [here](https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/com.jkuester.unlauncher.yml).

A list of all recent F-Droid builds can be found [here](https://f-droid.org/wiki/index.php?title=Special:RecentChanges&days=30&from=&hidebots=0&hideanons=1&hideliu=1&limit=500).
