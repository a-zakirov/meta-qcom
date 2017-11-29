DESCRIPTION = "Linaro pre-built toolchain for baremetal arm targets"
HOMEPAGE = "http://release.linaro.org/components/toolchain/binaries"
LICENSE = "GPL-3.0-with-GCC-exception & GPLv3"

def get_basever(d):
    pvcomponents = d.getVar('PV').split('-')
    v = pvcomponents[0].split('.')
    return v[0] + '.' + v[1] + '-' + pvcomponents[1]

BASEV = "${@get_basever(d)}"
SRC_URI = "http://releases.linaro.org/components/toolchain/binaries/${BASEV}/arm-eabi/gcc-linaro-${PV}-${BUILD_ARCH}_arm-eabi.tar.xz"
SRC_URI[md5sum] = "91b7d9efa1d8bac6646bd9295e7a35b7"
SRC_URI[sha256sum] = "93a937fc43d954500bdbcc1790b6257fca25ff55121acc2c92b2255420b7685c"
LIC_FILES_CHKSUM = "file://share/doc/gcc/Copying.html;md5=44c243d0b6787ba02843896b0831df23"

S = "${WORKDIR}/gcc-linaro-${PV}-${BUILD_ARCH}_arm-eabi"

inherit native

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}${datadir}/gcc-linaro-arm-eabi
    cp -R --no-dereference --preserve=links,mode ${S}/* ${D}/${datadir}/gcc-linaro-arm-eabi/
}

INHIBIT_SYSROOT_STRIP = "1"
