import Dependencies._

lazy val root = (project in file(".")).
  enablePlugins(JavaAgent).
  settings(
    inThisBuild(List(
      organization :=  "io.moia",
      scalaVersion :=  "2.12.6",
      version      :=  "0.1.0-SNAPSHOT",
      javaOptions  ++= globalJavaOptions
      //fork in Test := true
    )),
    name := "kamon-akka-streams-test-bed",
    libraryDependencies ++= Seq(
      akkaHttp % Compile,
      akkaStream % Compile,
      enumeratum % Compile,
      kamon % Compile,
      log4jApi % Compile,
      log4jCore % Compile,
      scalaLogging % Compile,
      logSlfOverLog4j % Compile,
      akkaActorTestkitTyped % Test,
      scalaTest % Test,
      scalaCheck % Test
    )
  )

javaAgents += "org.aspectj" % "aspectjweaver" % "1.8.13" % "test"

lazy val globalJavaOptions = Seq(
  // Suggested by kamon: http://kamon.io/documentation/1.x/recipes/adding-the-aspectj-weaver/
  "-Dorg.aspectj.tracing.factory=default",
  // Use kamon context to read the Logging context
  "-Dlog4j2.contextDataInjector=io.moia.kamon.log4j2.KamonContextDataInjector"
)