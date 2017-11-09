require recipes-devtools/gcc/gcc-${PV}.inc

# save these before native.bbclass munges them
root_prefix_orig := "${root_prefix}"
prefix_orig := "${target_prefix}"
exec_prefix_orig := "${exec_prefix}"
includedir_orig := "${includedir}"

inherit native
DEPENDS = "binutils-arm-none-eabi-native"
require gcc-arm-none-eabi.inc

PN = "gcc-arm-none-eabi-native"
DEPENDS = "binutils-arm-none-eabi-native ${NATIVEDEPS}"
MULTIMACH_TARGET_SYS = "${BUILD_ARCH}${BUILD_VENDOR}-${BUILD_OS}"
TARGET_ARCH[vardepvalue] = "${TARGET_ARCH}"

require recipes-devtools/gcc/gcc-configure-common.inc

EXTRA_OEMAKE += "inhibit_libc=true"

EXTRA_OECONF_PATHS = "\
    --with-gxx-include-dir=/not/exist${target_includedir}/c++/${BINV} \
    --with-sysroot=/not/exist \
    --with-build-sysroot=${STAGING_DIR_TARGET} \
"

ARCH_FLAGS_FOR_TARGET += "-isystem${STAGING_DIR_TARGET}${target_includedir}"

target_base_prefix := "${root_prefix_orig}"
target_prefix := "${prefix_orig}"
target_exec_prefix := "${exec_prefix_orig}"
target_base_libdir = "${target_base_prefix}/${baselib}"
target_libdir = "${target_exec_prefix}/${baselib}"
target_includedir := "${includedir_orig}"

# Overrides for paths
TARGET_PREFIX = "${TARGET_SYS}-"
STAGING_DIR_TARGET = "${RECIPE_SYSROOT}"
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

do_compile () {
	export CC="${BUILD_CC}"
	export AR_FOR_TARGET="${TARGET_SYS}-ar"
	export RANLIB_FOR_TARGET="${TARGET_SYS}-ranlib"
	export LD_FOR_TARGET="${TARGET_SYS}-ld"
	export NM_FOR_TARGET="${TARGET_SYS}-nm"
	export CC_FOR_TARGET="${CCACHE} ${TARGET_SYS}-gcc"
	export CFLAGS_FOR_TARGET="${TARGET_CFLAGS}"
	export CPPFLAGS_FOR_TARGET="${TARGET_CPPFLAGS}"
	export CXXFLAGS_FOR_TARGET="${TARGET_CXXFLAGS}"
	export LDFLAGS_FOR_TARGET="${TARGET_LDFLAGS}"

	oe_runmake all-host configure-target-libgcc
	(cd ${B}/${TARGET_SYS}/libgcc; oe_runmake enable-execute-stack.c unwind.h md-unwind-support.h sfp-machine.h gthr-default.h)
}

INHIBIT_PACKAGE_STRIP = "1"

# Compute how to get from libexecdir to bindir in python (easier than shell)
BINRELPATH = "${@os.path.relpath(d.expand("${STAGING_DIR_NATIVE}${prefix_native}/bin/${TARGET_SYS}"), d.expand("${libexecdir}/gcc/${TARGET_SYS}/${BINV}"))}"

do_install () {
	( cd ${B}/${TARGET_SYS}/libgcc; oe_runmake 'DESTDIR=${D}' install-unwind_h-forbuild install-unwind_h )
	oe_runmake 'DESTDIR=${D}' install-host

	install -d ${D}${target_base_libdir}
	install -d ${D}${target_libdir}

	# Link gfortran to g77 to satisfy not-so-smart configure or hard coded g77
	# gfortran is fully backwards compatible. This is a safe and practical solution. 
	if [ -n "${@d.getVar('FORTRAN')}" ]; then
		ln -sf ${STAGING_DIR_NATIVE}${prefix_native}/bin/${TARGET_PREFIX}gfortran ${STAGING_DIR_NATIVE}${prefix_native}/bin/${TARGET_PREFIX}g77 || true
		fortsymlinks="g77 gfortran"
	fi

	# Insert symlinks into libexec so when tools without a prefix are searched for, the correct ones are
	# found. These need to be relative paths so they work in different locations.
	dest=${D}${libexecdir}/gcc/${TARGET_SYS}/${BINV}/
	install -d $dest
	for t in ar as ld ld.bfd ld.gold nm objcopy objdump ranlib strip gcc cpp $fortsymlinks; do
		ln -sf ${BINRELPATH}/${TARGET_PREFIX}$t $dest$t
		ln -sf ${BINRELPATH}/${TARGET_PREFIX}$t ${dest}${TARGET_PREFIX}$t
	done

	# Remove things we don't need but keep share/java
	for d in info man share/doc share/locale share/man share/info; do
		rm -rf ${D}${STAGING_DIR_NATIVE}${prefix_native}/$d
	done

	# libquadmath headers need to  be available in the gcc libexec dir
	install -d ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/include/
	cp ${S}/libquadmath/quadmath.h ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/include/
	cp ${S}/libquadmath/quadmath_weak.h ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/include/

	# We use libiberty from binutils
	find ${D}${exec_prefix}/lib -name libiberty.a | xargs rm -f
	find ${D}${exec_prefix}/lib -name libiberty.h | xargs rm -f

	hardlinkdir . ${D}${includedir}/gcc-build-internal-${TARGET_SYS}
}

BUILDDIRSTASH = "${WORKDIR}/stashed-builddir"
do_gcc_stash_builddir[dirs] = "${B}"
do_gcc_stash_builddir[cleandirs] = "${BUILDDIRSTASH}"
do_gcc_stash_builddir () {
	dest=${BUILDDIRSTASH}
	hardlinkdir . $dest
}
addtask do_gcc_stash_builddir after do_compile before do_install
SSTATETASKS += "do_gcc_stash_builddir"
do_gcc_stash_builddir[sstate-inputdirs] = "${BUILDDIRSTASH}"
do_gcc_stash_builddir[sstate-outputdirs] = "${COMPONENTS_DIR}/${BUILD_ARCH}/gcc-stashed-builddir${COMPILERINITIAL}-${TARGET_SYS}"
do_gcc_stash_builddir[sstate-fixmedir] = "${COMPONENTS_DIR}/${BUILD_ARCH}/gcc-stashed-builddir${COMPILERINITIAL}-${TARGET_SYS}"

python do_gcc_stash_builddir_setscene () {
    sstate_setscene(d)
}
addtask do_gcc_stash_builddir_setscene
