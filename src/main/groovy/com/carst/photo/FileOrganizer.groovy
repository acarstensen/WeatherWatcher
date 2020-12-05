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

class FileOrganizer {
    Logger log = LoggerFactory.getLogger(this.class)

    static void main(String[] args) {
        FileOrganizer fo = new FileOrganizer()
        fo.go()
    }

    /*
        ToDo
            - Copy pics from hard drive to year folders
            - unzip zip files to appropriate folders
            - remove duplicates
            - Check with Mandy about these folders:
                - From Mom - Look Through and Sort
                - Mandy's Stuff
                - PhoneMandys
     */
    void go(){
        File baseDir = new File('C:\\Users\\HP.DESKTOP-U24D9IB\\Pictures\\Other22222')
        File picsDir = new File('C:/Users/HP.DESKTOP-U24D9IB/Pictures')
        File otherDir = new File('C:/Users/HP.DESKTOP-U24D9IB/Pictures/Other')
        List<String> fileTypes = ['.jpg', '.JPG', '.jpeg', '.png', '.raw', '.gif', '.PNG', '.bmp', '.wmv', '.MOV', '.MP4', '.mp4','.mov','.avi','.AVI']

        File listOfHashesFile = new File('C:/Users/HP.DESKTOP-U24D9IB/Pictures/FilesCopiedFromHardDrive_AVIs.txt')
        FileUtils.writeLines(listOfHashesFile, ["Hash|srcFile|destFile"], true)

        baseDir.eachFileRecurse { File f ->
            if(!f.isDirectory()) {
                println "Copying: ${f.absolutePath}"

                // get file name pieces
                String fileType = f.name.substring(f.name.lastIndexOf('.'))
                String fileName = f.name.substring(0, f.name.lastIndexOf('.'))

                // determine pics or other directory
                File destDir = picsDir
                if(!fileTypes.contains(fileType)) {
                    destDir = otherDir
                }

                // determine year directory
                String createdYear = new Date(f.lastModified()).format('yyyy')
                destDir = new File("${destDir.absolutePath}/${createdYear}")
                destDir.mkdirs()

                // find unique name
                File destFile = new File("${destDir.absolutePath}/${fileName}${fileType}")
                for(int x=1; x<1000; x++) {
                    if(destFile.exists()) {
                        destFile = new File("${destDir.absolutePath}/${fileName}_${x}${fileType}")
                    } else {
                        x=1000 // exit loop
                    }
                }

                // copy file
                FileUtils.moveFile(f, destFile)

                // write out hash
                FileUtils.writeLines(listOfHashesFile, ["${generateMD5(destFile)}|${f.absolutePath}|${destFile.absolutePath}"], true)

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
