module com.adrienben.games.bagl.tests {
    requires java.base;
    requires org.lwjgl;
    requires org.lwjgl.natives;
    requires org.lwjgl.glfw;
    requires org.lwjgl.glfw.natives;
    requires org.lwjgl.opengl;
    requires org.lwjgl.opengl.natives;
    requires org.apache.logging.log4j;
    requires org.junit.jupiter.api;

    exports com.adrienben.games.bagl.tests;
}