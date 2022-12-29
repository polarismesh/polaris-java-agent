package cn.polarismesh.agent.plugin.nacos.asm;


import org.objectweb.asm.*;



public interface AsmDynamicClassGenerator extends Opcodes {

    byte[] generate();

}