package com.codelang.parse.bean;

import java.util.List;

public class ConfigBean {
    private List<String> stringRef;
    private List<Method> methodRef;
    private List<Feild> fieldRef;

    public List<String> getStringRef() {
        return stringRef;
    }

    public void setStringRef(List<String> stringRef) {
        this.stringRef = stringRef;
    }

    public List<Feild> getFieldRef() {
        return fieldRef;
    }

    public void setFieldRef(List<Feild> fieldRef) {
        this.fieldRef = fieldRef;
    }


    public List<Method> getMethodRef() {
        return methodRef;
    }

    public void setMethodRef(List<Method> methodRef) {
        this.methodRef = methodRef;
    }

    public static class Method {
        private String className;
        private String method;
        private String signature;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }
    }

    public static class Feild {
        private String className;
        private String fieldName;
        private String signature;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        @Override
        public String toString() {
            return "Feild{" +
                    "className='" + className + '\'' +
                    ", fieldName='" + fieldName + '\'' +
                    ", signature='" + signature + '\'' +
                    '}';
        }
    }
}
