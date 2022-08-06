package com.padingpading.interview.servial;

import com.padingpading.interview.context.SysConstant;
import lombok.Data;

import java.io.Externalizable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * readResolve：反序列化时替换反序列化出的对象，反序列化出来的对象被立即丢弃。此方法在readeObject后调用。
 */
public class I_Externalizable {
    
    public static void main(String[] args) throws Exception {
        //序列化
        String path = SysConstant.SERIAL_PATH + "I_Externalizable.txt";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
                ObjectInputStream ios = new ObjectInputStream(new FileInputStream(path))) {
            oos.writeObject(new ExPerson("brady", 23));
            ExPerson ep = (ExPerson) ios.readObject();
            System.out.println(ep);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Data
    static class ExPerson implements Externalizable {
        
        private String name;
        
        private int age;
        
        //注意，必须加上pulic 无参构造器
        public ExPerson() {
        }
        
        public ExPerson(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            //将name反转后写入二进制流
            StringBuffer reverse = new StringBuffer(name).reverse();
            System.out.println(reverse.toString());
            out.writeObject(reverse);
            out.writeInt(age);
            
        }
        
        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            //将读取的字符串反转后赋值给name实例变量
            this.name = ((StringBuffer) in.readObject()).reverse().toString();
            System.out.println(name);
            this.age = in.readInt();
        }
    }
}