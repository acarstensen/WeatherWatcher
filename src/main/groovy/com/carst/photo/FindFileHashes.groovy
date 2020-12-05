/*
 * Copyright Medtronic, Inc. 2016
 *
 * MEDTRONIC CONFIDENTIAL - This document is the property of Medtronic,
 * Inc., and must be accounted for. Information herein is confidential. Do
 * not reproduce it, reveal it to unauthorized persons, or send it outside
 * Medtronic without proper authorization.
 */

package com.carst.photo

import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.security.DigestInputStream
import java.security.MessageDigest

class FindFileHashes {
    Logger log = LoggerFactory.getLogger(this.class)

    static void main(String[] args) {
        FindFileHashes fo = new FindFileHashes()
        fo.go()
    }

    void go(){
        File picsDir = new File('C:/Users/HP.DESKTOP-U24D9IB/Pictures/Pictures')
        File listOfHashesFile = new File('C:/Users/HP.DESKTOP-U24D9IB/Pictures/aaaaa.txt')
        FileUtils.writeLines(listOfHashesFile, ["Hash|Path"], true)

        picsDir.eachFileRecurse { File f ->
            if(!f.isDirectory()) {
                if(f.name != 'desktop.ini') {
                    def hash = generateMD5(f)
                    FileUtils.writeLines(listOfHashesFile, ["${hash}|${f.absolutePath}"], true)
                    println f.absolutePath
                }
            }
        }
    }

    def generateMD5(File file) {
        file.withInputStream {
            new DigestInputStream(it, MessageDigest.getInstance('MD5')).withStream {
                it.eachByte {}
                it.messageDigest.digest().encodeHex() as String
            }
        }
    }
}
