package org.scalafmt.cli

import java.io.File

import org.scalafmt.Error.InvalidScalafmtConfiguration
import org.scalafmt.Formatted
import org.scalafmt.Scalafmt
import org.scalafmt.config.ScalafmtRunner
import org.scalafmt.config.ScalafmtConfig
import org.scalafmt.util.LoggerOps._

/**
  * Classload ScalaFmt210 to run ScalaFmt from Scala 2.10, for example sbt
  * plugin.
  */
class Scalafmt210 {

  def format(code: String, configFile: String, filename: String): String = {
    val style = StyleCache
      .getStyleForFile(configFile)
      .getOrElse(
        throw InvalidScalafmtConfiguration(new File(configFile))
      )
    format(code, style, filename)
  }

  def format(code: String, filename: String): String =
    format(code, ScalafmtConfig.default, filename)

  private def format(code: String,
                     scalafmtStyle: ScalafmtConfig,
                     filename: String): String = {
    val currentPath = new File("").getAbsolutePath + "/"
    val relativePath = filename.stripPrefix(currentPath)
    val runner = // DRY please, same login in CLI
      if (filename.endsWith(".sbt")) ScalafmtRunner.sbt
      else ScalafmtRunner.default
    val style =scalafmtStyle.copy(runner = runner)
    Scalafmt.format(code, style) match {
      case Formatted.Success(formattedCode) => formattedCode
      case error =>
        error match {
          case Formatted.Failure(e) =>
            logger.warn(
              s"Failed to format file $relativePath. Cause: ${e.getMessage}.")
          case _ =>
        }
        code
    }
  }
}
