require recipes-devtools/binutils/binutils.inc
require recipes-devtools/binutils/binutils-${PV}.inc
inherit native

FILESEXTRAPATHS_prepend := "${COREBASE}/meta/recipes-devtools/binutils/binutils:"

MULTIMACH_TARGET_SYS = "${BUILD_ARCH}${BUILD_VENDOR}-${BUILD_OS}"
TARGET_ARCH[vardepvalue] = "${TARGET_ARCH}"
SRC_URI += "file://0002-binutils-cross-Do-not-generate-linker-script-directo.patch"

# Overrides for paths
TARGET_PREFIX = "${TARGET_SYS}-"
CROSS_TARGET_SYS_DIR = "${TARGET_SYS}"
prefix = "${STAGING_DIR_NATIVE}${prefix_native}"
base_prefix = "${STAGING_DIR_NATIVE}"
exec_prefix = "${STAGING_DIR_NATIVE}${prefix_native}"
bindir = "${exec_prefix}/bin/${CROSS_TARGET_SYS_DIR}"
sbindir = "${bindir}"
base_bindir = "${bindir}"
base_sbindir = "${bindir}"
libdir = "${exec_prefix}/lib/${CROSS_TARGET_SYS_DIR}"
libexecdir = "${exec_prefix}/libexec/${CROSS_TARGET_SYS_DIR}"

# Specify lib-path else we use a load of search dirs which we don't use
# and mean the linker scripts have to be relocated.
EXTRA_OECONF += "--with-sysroot=${STAGING_DIR_TARGET} \
                --disable-install-libbfd \
                --enable-poison-system-directories \
                --with-lib-path==${target_base_libdir}:=${target_libdir} \
                "
do_install () {
	oe_runmake 'DESTDIR=${D}' install

	# We don't really need these, so we'll remove them...
	rm -rf ${D}${STAGING_DIR_NATIVE}${libdir_native}/libiberty.a
	rm -rf ${D}${STAGING_DIR_NATIVE}${prefix_native}/${TARGET_SYS}
	rm -rf ${D}${STAGING_DIR_NATIVE}${prefix_native}/lib/ldscripts
	rm -rf ${D}${STAGING_DIR_NATIVE}${prefix_native}/share/info
	rm -rf ${D}${STAGING_DIR_NATIVE}${prefix_native}/share/locale
	rm -rf ${D}${STAGING_DIR_NATIVE}${prefix_native}/share/man
	rmdir ${D}${STAGING_DIR_NATIVE}${prefix_native}/share || :
	rmdir ${D}${STAGING_DIR_NATIVE}${prefix_native}/${libdir}/gcc-lib || :
	rmdir ${D}${STAGING_DIR_NATIVE}${prefix_native}/${libdir}64/gcc-lib || :
	rmdir ${D}${STAGING_DIR_NATIVE}${prefix_native}/${libdir} || :
	rmdir ${D}${STAGING_DIR_NATIVE}${prefix_native}/${libdir}64 || :
	rmdir ${D}${STAGING_DIR_NATIVE}${prefix_native}/${prefix} || :
}

PROVIDES = ""
PN = "binutils-arm-none-eabi-native"
BPN = "binutils"
TARGET_ARCH = "arm"
TARGET_VENDOR = "-none"
TARGET_VENDOR_virtclass-multilib-lib32 = "-none"
TARGET_OS = "eabi"

