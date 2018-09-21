// Add aspectJ weaver to run
resolvers += Resolver.bintrayRepo("kamon-io", "sbt-plugins")
addSbtPlugin("io.kamon" % "sbt-aspectj-runner" % "1.1.0")

// Add ability to add java agent options for native packager
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.4")
