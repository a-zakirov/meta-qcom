require recipes-devtools/gcc/gcc-${PV}.inc
require recipes-devtools/gcc/gcc-runtime.inc
require gcc-arm-none-eabi.inc

RUNTIMETARGET = "libgcc"

DEPENDS = "gcc-cross-arm-none-eabi"
COMPILERDEP = "gcc-cross-arm-none-eabi:do_gcc_stash_builddir"
PN = "gcc-runtime-arm-none-eabi"

do_install_append() {
	# Hoist libgcc up into the directory where the compiler will look for it
	mv ${D}${libdir}/gcc/${TARGET_SYS} ${D}${libdir}/
	rmdir ${D}${libdir}/gcc
}

PACKAGES = "${PN}-dev"
FILES_${PN}-dev = "${libdir}/${TARGET_SYS}"

python __anonymous() {
    for p in ['deb', 'rpm', 'ipk']:
        deps = d.getVarFlag('do_package_write_%s' % p, 'depends', False).split()
        newdeps = [dep for dep in deps if dep != "virtual/${MLPREFIX}libc:do_packagedata"]
        d.setVarFlag('do_package_write_%s' % p, 'depends', ' '.join(newdeps))
}
