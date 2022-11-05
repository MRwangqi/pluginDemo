package classfile;

/**
 * 将签名转成可读形式
 *
 * @Stark
 */
public class Signature2Readable {
    private String desc = "";

    public Signature2Readable(String desc) {
        this.desc = desc;
    }

    public static String method(String name, String desc) {
        Signature2Readable readable = new Signature2Readable(desc);
        String result = null;
        try {
            result = new StringBuilder(readable.getReturnType()).append(" ").append(name).append(readable.getParameterTypes()).toString();
        } catch (Exception e) {
            e.printStackTrace();
            result = name + " " + desc;
        }
        return result;
    }

    public static String field(String name,String desc){
        Signature2Readable readable = new Signature2Readable(desc);
        String result = null;
        try {
            result = new StringBuilder(readable.getFieldType()).append(" ").append(name).toString();
        }catch (Exception e){
            e.printStackTrace();
            result = desc + " " + name;
        }
        return result;
    }

    public String getParameterTypes()
            throws Descriptor.InvalidDescriptor {
        int end = desc.indexOf(")");
        if (end == -1)
            throw new Descriptor.InvalidDescriptor(desc);
        return parse(desc, 0, end + 1);
    }

    public String getReturnType()
            throws Descriptor.InvalidDescriptor {
        int end = desc.indexOf(")");
        if (end == -1)
            throw new Descriptor.InvalidDescriptor(desc);
        return parse(desc, end + 1, desc.length());
    }

    public String getFieldType()
            throws Descriptor.InvalidDescriptor {
        return parse(desc, 0, desc.length());
    }

    private int count = 0;

    private String parse(String desc, int start, int end)
            throws Descriptor.InvalidDescriptor {
        int p = start;
        StringBuilder sb = new StringBuilder();
        int dims = 0;
        count = 0;

        while (p < end) {
            String type;
            char ch;
            switch (ch = desc.charAt(p++)) {
                case '(':
                    sb.append('(');
                    continue;

                case ')':
                    sb.append(')');
                    continue;

                case '[':
                    dims++;
                    continue;

                case 'B':
                    type = "byte";
                    break;

                case 'C':
                    type = "char";
                    break;

                case 'D':
                    type = "double";
                    break;

                case 'F':
                    type = "float";
                    break;

                case 'I':
                    type = "int";
                    break;

                case 'J':
                    type = "long";
                    break;

                case 'L':
                    int sep = desc.indexOf(';', p);
                    if (sep == -1)
                        throw new Descriptor.InvalidDescriptor(desc, p - 1);
                    type = desc.substring(p, sep).replace('/', '.');
                    p = sep + 1;
                    break;

                case 'S':
                    type = "short";
                    break;

                case 'Z':
                    type = "boolean";
                    break;

                case 'V':
                    type = "void";
                    break;

                default:
                    throw new Descriptor.InvalidDescriptor(desc, p - 1);
            }

            if (sb.length() > 1 && sb.charAt(0) == '(')
                sb.append(", ");
            sb.append(type);
            for (; dims > 0; dims--)
                sb.append("[]");

            count++;
        }

        return sb.toString();
    }
}
