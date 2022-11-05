package com.codelang.parse.bean;

import java.util.ArrayList;
import java.util.List;

public class ResponseBean {

    public List<Permission> stringRef ;
    public List<Method> methodRef;
    public List<Field> fieldRef;


    public static class Permission {
        private String name;
        private List<Ref> ref = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Ref> getRef() {
            return ref;
        }

        public void setRef(List<Ref> ref) {
            this.ref = ref;
        }
    }

    public static class Method {
        private String className;
        private String method;
        private String signature;
        private List<Ref> ref = new ArrayList<>();

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

        public List<Ref> getRef() {
            return ref;
        }

        public void setRef(List<Ref> ref) {
            this.ref = ref;
        }
    }


    public static class Field {
        private String className;
        private String fieldName;
        private String signature;
        private List<Ref> ref = new ArrayList<>();

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

        public List<Ref> getRef() {
            return ref;
        }

        public void setRef(List<Ref> ref) {
            this.ref = ref;
        }
    }

    public static class Ref {
        private String dependencies;
        private String className;
        private String method;

        public String getDependencies() {
            return dependencies;
        }

        public void setDependencies(String dependencies) {
            this.dependencies = dependencies;
        }

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
    }
}
