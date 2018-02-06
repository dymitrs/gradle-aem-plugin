package com.cognifide.gradle.aem.instance.satisfy

import aQute.bnd.osgi.Jar
import com.cognifide.gradle.aem.api.AemConfig
import com.cognifide.gradle.aem.internal.file.FileOperations
import com.cognifide.gradle.aem.internal.file.resolver.FileResolution
import com.cognifide.gradle.aem.pkg.PackagePlugin
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.gradle.util.GFileUtils
import org.jsoup.nodes.Element
import org.zeroturnaround.zip.ZipUtil
import java.io.File

class PackageResolution(group: PackageGroup, id: String, action: (FileResolution) -> File) : FileResolution(group, id, action) {

    val config = AemConfig.of(group.resolver.project)

    val logger = group.resolver.project.logger

    override fun process(file: File): File {
        val origin = super.process(file)

        return when (FilenameUtils.getExtension(file.name)) {
            "jar" -> wrap(origin)
            "zip" -> origin
            else -> throw PackageException("File $origin must have *.jar or *.zip extension")
        }
    }

    private fun wrap(jar: File): File {
        val pkgName = jar.nameWithoutExtension
        val pkg = File(dir, "$pkgName.zip")
        if (pkg.exists()) {
            logger.info("CRX package wrapping OSGi bundle already exists: $pkg")
            return pkg
        }

        logger.info("Wrapping OSGi bundle to CRX package: $jar")

        val pkgRoot = File(dir, pkgName)
        val pkgPath = "${config.satisfyBundlePath}/${jar.name}"
        val vaultDir = File(pkgRoot, PackagePlugin.VLT_PATH)

        // Copy package template files
        GFileUtils.mkdirs(vaultDir)
        FileOperations.copyResources(PackagePlugin.VLT_PATH, vaultDir)

        // Expand package properties
        val bundle = Jar(jar)
        val description = bundle.manifest.mainAttributes.getValue("Bundle-Description") ?: ""
        val symbolicName = bundle.manifest.mainAttributes.getValue("Bundle-SymbolicName")
        val group = symbolicName.substringBeforeLast(".")
        val version = bundle.manifest.mainAttributes.getValue("Bundle-Version")

        val defaultProps = mapOf<String, Any>(
                "project.group" to group,
                "project.name" to symbolicName,
                "project.version" to version,
                "project.description" to description,
                "config.packageName" to symbolicName,
                "config.acHandling" to config.acHandling,
                "filters" to listOf(Element("<filter root=\"$pkgPath\"/>"))
        )
        val commonProps = config.fileProperties
        val overrideProps = config.satisfyBundleProperties(bundle)
        val effectiveProps = defaultProps + commonProps + overrideProps

        FileOperations.amendFiles(vaultDir, config.filesExpanded, { file, line ->
            config.propParser.expand(line, effectiveProps, file.absolutePath)
        })

        // Copy bundle to install path
        val pkgJar = File(pkgRoot,"jcr_root$pkgPath")

        GFileUtils.mkdirs(pkgJar.parentFile)
        FileUtils.copyFile(jar, pkgJar)

        // ZIP all to CRX package
        ZipUtil.pack(pkgRoot, pkg)
        pkgRoot.delete()

        return pkg
    }
}