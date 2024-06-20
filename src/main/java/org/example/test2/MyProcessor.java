package org.example.test2;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import org.example.test2.annotation.FieldJson;
import org.example.test2.annotation.TypeJson;
import org.example.test2.IFormat;
import org.json.JSONObject;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

@SupportedAnnotationTypes({"org.example.test2.annotation.TypeJson", "org.example.test2.annotation.FieldJson"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class MyProcessor extends AbstractProcessor {
    private Messager messager;
    private Elements elementUtils;
    private Filer filer;
    private List<TypeElement> elementList;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        System.out.println("注解器 init");
        this.elementUtils = processingEnv.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        messager.printMessage(Diagnostic.Kind.NOTE, "init");
        elementList = new ArrayList<>();
    }
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, "process");
        System.out.println("2312312");
        for (Element element : roundEnv.getElementsAnnotatedWith(TypeJson.class)) {
            TypeElement typeElement = (TypeElement) element;
            try {
                generateFormat(typeElement);
                elementList.add(typeElement);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        generateFormatManager();
        return true;
    }

    private void generateFormatManager() {
        String packageName = "org.example.test2.format";

        CodeBlock.Builder codeBlock = CodeBlock.builder();
        for (TypeElement typeElement : elementList){
            String clz = typeElement.getClass().getSimpleName();
            codeBlock.addStatement("map.put($L, new $LFormat())", clz, clz);
        }

        TypeVariableName typeVariableT = TypeVariableName.get("T");

        FieldSpec myField = FieldSpec
                .builder(ParameterizedTypeName.get(Map.class, Type.class, IFormat.class), "map")
                .addModifiers(Modifier.FINAL, Modifier.PRIVATE, Modifier.STATIC)
                .initializer("new $T<>()", HashMap.class)
                .build();

        MethodSpec format1 = MethodSpec.methodBuilder("format")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .addTypeVariable(typeVariableT)
                .addParameter(String.class, "str")
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), typeVariableT), "type")
                .addStatement("return format(new JSONObject(str), type)")
                .returns(typeVariableT)
                .build();

        MethodSpec format2 = MethodSpec.methodBuilder("format")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .addTypeVariable(typeVariableT)
                .addParameter(JSONObject.class, "jsonObject")
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), typeVariableT), "type")
                .addStatement("return (T) map.get(type).format(jsonObject)")
                .returns(typeVariableT)
                .build();

        TypeSpec myClass = TypeSpec.classBuilder("FormatManager")
                .addModifiers(Modifier.PUBLIC)
                .addField(myField)
                .addStaticBlock(codeBlock.build())
                .addMethod(format1)
                .addMethod(format2)
                .build();

        try {
            JavaFile.builder(packageName, myClass).build().writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateFormat(TypeElement typeElement) throws NoSuchFieldException, IOException {
        String packageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
        String typeName = typeElement.getSimpleName().toString();
        String newName = typeName + "Format";

        ClassName targetName = ClassName.get(typeElement);
        // IFormat 接口名
        TypeElement iFormatElement = elementUtils.getTypeElement("org.example.test2.format.IFormat");
        ClassName iFormatClassName = ClassName.get(iFormatElement);
        // 该类下的所有字段的映射
        Map<String, String> map = new HashMap<>();
        Field[] fields = typeElement.getClass().getDeclaredFields();
        for (Field field : fields){
            if (field.isAnnotationPresent(FieldJson.class)){
                map.put(field.getAnnotation(FieldJson.class).value(), field.getName());
            } else {
                map.put(field.getName(), field.getName());
            }
        }

        FieldSpec myField = FieldSpec.builder(ParameterizedTypeName.get(Map.class, String.class, String.class), "map")
                .addModifiers(Modifier.FINAL, Modifier.PRIVATE)
                .initializer("new $T<>()", HashMap.class)
                .build();

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("myMap.putAll($L)", map)
                .build();

        MethodSpec.Builder format = MethodSpec.methodBuilder("format")
                .addModifiers(Modifier.PUBLIC)
                .returns(typeElement.getClass())
                .addParameter(JSONObject.class, "jsonObject")
                .addStatement("$T obj = new $T()", typeElement.getClass(), typeElement.getClass())
                .beginControlFlow("for ($T e : map.entrySet())", ParameterizedTypeName.get(Map.Entry.class, String.class, String.class));

        for (Map.Entry<String, String> entry : map.entrySet()){
            String v = entry.getValue();
            Class<?> clz = typeElement.getClass().getField(v).getClass();
            String upperValue = v.substring(0,1).toUpperCase() + v.substring(1);
            String upperClassName = clz.getSimpleName();
            upperClassName = upperClassName.substring(0,1).toUpperCase() + upperClassName.substring(1);

            format.beginControlFlow("if ($S.equals(e.getKey()))", entry.getValue());
            if (clz.isPrimitive() || clz.equals(String.class)){
                format.endControlFlow("obj.set$L(jsonObjet.opt$L(e.getKey()))", upperValue, upperClassName);
            } else {
                format.endControlFlow("obj.set$L(FormatManager.format(jsonObject.optJSONObject(e.getKey()), $L.class)))", upperValue, upperClassName);
            }
        }

        TypeSpec myClass = TypeSpec.classBuilder(newName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(iFormatClassName, targetName))
                .addField(myField)
                .addMethod(constructor)
                .addMethod(format.endControlFlow().build())
                .build();

        JavaFile.builder(packageName, myClass).build().writeTo(filer);
    }
}
