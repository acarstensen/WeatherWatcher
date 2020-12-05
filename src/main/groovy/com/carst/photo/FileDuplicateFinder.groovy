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

class FileDuplicateFinder {
    Logger log = LoggerFactory.getLogger(this.class)

    static void main(String[] args) {
        FileDuplicateFinder fo = new FileDuplicateFinder()
        fo.go()
    }

    void go(){
        File picsDir = new File('C:/Users/HP.DESKTOP-U24D9IB/Pictures/Pictures')
        File uniquePicsDir = new File('C:/Users/HP.DESKTOP-U24D9IB/Pictures/Pictures/uniquePics')
        def files = [:]
        def hashes = []

        picsDir.eachFileRecurse { File f ->
            if(!f.isDirectory()) {
                if(f.name != 'desktop.ini') {
                    def hash = generateMD5(f)
                    files.put(f.absolutePath, hash)
                    hashes.push(hash)
                    println "Hash: ${hash} Path: ${f.absolutePath}"
                }
            }
        }

        log.info("Finding duplicate hashes...")
        Set<String> dupHashes = []
        hashes = hashes.sort()
        for(int x=0; x<hashes.size(); x++) {
            String hash1 = hashes.get(x)
            for(int y=x+1; y<hashes.size(); y++) {
                String hash2 = hashes.get(y)
                if(hash1 != hash2) {
                    y=hashes.size() // exit
                } else {
                    dupHashes.add(hash2)
                }
            }
        }

        log.info("Moving files to unique directory...")
        List<String> dupHashesAlreadyMoved = []
        files.each {String path, String hash ->
            boolean moveFile = false
            if(dupHashes.contains(hash)) {
                if(!dupHashesAlreadyMoved.contains(hash)) {
                    moveFile = true
                    dupHashesAlreadyMoved.add(hash)
                }
            } else {
                moveFile = true // not a duplicate so go ahead and move
            }

            if(moveFile) {
                FileUtils.moveFileToDirectory(new File(path), uniquePicsDir, false)
            } else {
             println "Duplicate file found: ${path}"
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
