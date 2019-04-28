package com.wix.bazel.migrator

import java.nio.file.{Files, Path}

class BazelRcRemoteSettingsWriter(repoRoot: Path) {

  def write(): Unit = {
    val contents =
      """# NOTE - THIS FILE IS MANUALLY DUPLICATED INSIDE WAZEL CONTAINER (see BazelRcRemoteSettingsWriter.writeToDisk for explanation)
        |
        |# Remote Build Execution requires a strong hash function, such as SHA256.
        |startup --host_jvm_args=-Dbazel.DigestFunction=SHA256
        |
        |# Set several flags related to specifying the toolchain and java properties.
        |# These flags are duplicated rather than imported from (for example)
        |# %workspace%/configs/debian8_clang/0.2.0/toolchain.bazelrc to make this
        |# bazelrc a standalone file that can be copied more easily.
        |build:rbe_based --host_javabase=@core_server_build_tools//rbe-toolchains/jdk:jdk8
        |build:rbe_based --javabase=@core_server_build_tools//rbe-toolchains/jdk:jdk8
        |build --crosstool_top=@core_server_build_tools//toolchains:crosstool_top
        |build --action_env=BAZEL_DO_NOT_DETECT_CPP_TOOLCHAIN=1
        |build --extra_toolchains=@core_server_build_tools//toolchains:extra_toolchains
        |build --host_platform=@core_server_build_tools//rbe-toolchains/jdk:rbe_ubuntu1604
        |build --platforms=@core_server_build_tools//rbe-toolchains/jdk:rbe_ubuntu1604
        |build:rbe_based --action_env=PLACE_HOLDER=SO_USING_CONFIG_GROUP_WILL_WORK_BW_CMPTBL
        |
        |# Enable encryption.
        |build --tls_enabled=true
        |
        |# Enforce stricter environment rules, which eliminates some non-hermetic
        |# behavior and therefore improves both the remote cache hit rate and the
        |# correctness and repeatability of the build.
        |build --experimental_strict_action_env=true
        |
        |# Set a higher timeout value, just in case.
        |build --remote_timeout=3600
        |
        |# Enable authentication. This will pick up application default credentials by
        |# default. You can use --auth_credentials=some_file.json to use a service
        |# account credential instead.
        |build --auth_enabled=true
        |
        |#The following environment variable is used by bazel integration e2e tests which need to know if we're using the
        |#`remote` configuration and so add custom toolchains which means the tests need to add them as well
        |test --test_env=REMOTE="true"
        |
        |test --test_env=CC
        |
        |build:rbe_based --extra_execution_platforms=@core_server_build_tools//platforms:rbe_small,@core_server_build_tools//platforms:rbe_large,@core_server_build_tools//platforms:rbe_default
        |test:rbe_based --extra_execution_platforms=@core_server_build_tools//platforms:rbe_small,@core_server_build_tools//platforms:rbe_large,@core_server_build_tools//platforms:rbe_default
        |
      """.stripMargin
    writeToDisk(contents)
  }

  // currently this file is duplicated between the global location (generated by the migrator) and between wazel container.
  // This is because docker cannot ADD files if they're not in the build context (symlinks included)
  // The global file is currently used for the jenkins rbe step AND gcb container (which runs rbe)
  // plan for removing this duplication - once we move to building all our images with docker-rules,
  // move .bazelrc.remotesettings to be a resource for both the gcb-bazel-step container AND for wazel container
  // (NOTE - if jenkins is still alive when this happens, it should also be added to the jenkins execution image)
  private def writeToDisk(contents: String): Unit =
    Files.write(repoRoot.resolve("tools/bazelrc/.bazelrc.remotesettings"), contents.getBytes)
}
