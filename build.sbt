/*
 * Copyright 2015 Imply Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

organization := "io.imply"

name := "wikiticker"

scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.10.5", "2.11.7")

net.virtualvoid.sbt.graph.Plugin.graphSettings

licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("https://github.com/implydata/wikiticker"))

publishMavenStyle := true

publishTo := Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2/")

pomIncludeRepository := { _ => false }

pomExtra := <scm>
  <url>https://github.com/implydata/wikiticker.git</url>
  <connection>scm:git:git@github.com:implydata/wikiticker.git</connection>
</scm>

releaseSettings

ReleaseKeys.publishArtifactsAction := PgpKeys.publishSigned.value

libraryDependencies ++= Seq(
  "com.metamx" %% "scala-util" % "1.11.3" exclude("log4j", "log4j") force(),
  "com.ircclouds.irc" % "irc-api" % "1.0-0014"
)

//
// Logging
//

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-core" % "1.1.2",
  "ch.qos.logback" % "logback-classic" % "1.1.2"
)

//
// Test stuff
//

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.5" % "test",
  "junit" % "junit" % "4.11" % "test",
  "com.novocode" % "junit-interface" % "0.11-RC1" % "test"
)
