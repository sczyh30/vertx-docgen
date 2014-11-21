package io.vertx.docgen;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@SupportedAnnotationTypes({
    "io.vertx.docgen.Document"
})
@javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_8)
public class DocGenProcessor extends BaseProcessor {

  @Override
  protected void handleGen(PackageElement moduleElt, String content) {
    String outputOpt = processingEnv.getOptions().get("docgen.output");
    if (outputOpt != null) {
      File outputDir = new File(outputOpt);
      if (outputDir.exists()) {
        if (outputDir.isDirectory()) {
          write(outputDir, moduleElt, content);
        } else {
          System.out.println("could not use non dir " + outputDir.getAbsolutePath());
        }
      } else {
        if (outputDir.mkdirs()) {
          write(outputDir, moduleElt, content);
        } else {
          System.out.println("could not create dir " + outputDir.getAbsolutePath());
        }
      }
    }
  }

  @Override
  protected String resolveLinkgPackageDoc(PackageElement elt) {
    return elt.toString() + ".adoc";
  }

  protected String resolveLinkTypeDoc(TypeElement elt) {
    return "apidocs/" + elt.getQualifiedName().toString().replace('.', '/') + ".html";
  }

  @Override
  protected String resolveLinkConstructorDoc(ExecutableElement elt) {
    return resolveLinkExecutableDoc(elt, elt.getEnclosingElement().getSimpleName().toString());
  }

  protected String resolveLinkMethodDoc(ExecutableElement elt) {
    return resolveLinkExecutableDoc(elt, elt.getSimpleName().toString());
  }

  private String resolveLinkExecutableDoc(ExecutableElement elt, String name) {
    TypeElement typeElt = (TypeElement) elt.getEnclosingElement();
    String link = resolveLinkTypeDoc(typeElt);
    StringBuilder anchor = new StringBuilder("#");
    anchor.append(name).append('-');
    TypeMirror type  = elt.asType();
    ExecutableType methodType  = (ExecutableType) processingEnv.getTypeUtils().erasure(type);
    List<? extends TypeMirror> parameterTypes = methodType.getParameterTypes();
    for (int i = 0;i < parameterTypes.size();i++) {
      if (i > 0) {
        anchor.append('-');
      }
      anchor.append(parameterTypes.get(i));
    }
    anchor.append('-');
    return link + anchor;
  }

  @Override
  protected String resolveLinkFieldDoc(VariableElement elt) {
    TypeElement typeElt = (TypeElement) elt.getEnclosingElement();
    String link = resolveLinkTypeDoc(typeElt);
    return link + "#" + elt.getSimpleName();
  }

  private void write(File dir, PackageElement moduleElt, String content) {
    try {
      File file = new File(dir, moduleElt.getQualifiedName().toString() + ".adoc");
      try (FileWriter writer = new FileWriter(file)) {
        writer.write(content);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
