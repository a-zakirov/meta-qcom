DESCRIPTION = "Qualcomm primary bootloader binary blobs for Dragonboard"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://license.txt;md5=c09af6bc68c68f92e6a711634ee5cb14"

SRC_URI = "http://builds.96boards.org/releases/dragonboard410c/linaro/rescue/${PV}/dragonboard410c_bootloader_emmc_linux-79.zip \
	   file://emmc-partitions.txt"
SRC_URI[md5sum] = "1cf7bc8704f1abb093be801420166593"
SRC_URI[sha256sum] = "037b855edbfa69ce1d6c70326e9566b7d48567f3923bf41bf492fd3f059248dd"

COMPATIBLE_MACHINE = "(apq8016)"

PACKAGE_ARCH = "${MACHINE_ARCH}"

S = "${WORKDIR}"
B = "${S}"

INHIBIT_DEFAULT_DEPS = "1"
DEPENDS = ""

do_configure() {
    :
}
do_compile() {
    :
}
do_install() {
    install -d ${D}${libdir}/${PN}
    for f in sbc_1.0_8016.bin hyp.mbn rpm.mbn tz.mbn sbl1.mbn LICENSE; do
	install -m 0644 $f ${D}${libdir}/${PN}/
    done
    install -m 0644 emmc-partitions.txt ${D}${libdir}/${PN}/partitions.txt
}

PACKAGES = "${PN}-dev ${PN}"
FILES_${PN}-dev = "${libdir}/${PN}"
INSANE_SKIP_${PN}-dev = "arch"
ALLOW_EMPTY_${PN} = "1"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_SYSROOT_STRIP = "1"
