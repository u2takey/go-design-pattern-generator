package template.pattern;

import com.goide.psi.*;
import com.goide.psi.impl.GoTypeUtil;
import com.goide.refactor.template.GoTemplate;
import com.goide.util.GoUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import template.DesignPattern;

import java.util.HashMap;


/**
 * @author leiiwang@tencent.com on 2021/3/19
 */
public class ConvertTemplate extends DesignPattern {
    private GoTypeSpec spec1;
    private GoTypeSpec spec2;

    public ConvertTemplate(@NotNull AnActionEvent event, @NotNull GoTypeSpec spec1,
                           @NotNull GoTypeSpec spec2) {
        super(event);
        this.spec1 = spec1;
        this.spec2 = spec2;
    }

    @Override
    protected void createTemplate(@NotNull GoTemplate template) {
        String packageName1 = this.spec1.getContainingFile().getPackageName();
        this.createTemplate(template, this.spec1, this.spec2, packageName1);
        this.createTemplate(template, this.spec2, this.spec1, packageName1);
    }

    protected void createTemplate(@NotNull GoTemplate template, @NotNull GoTypeSpec spec1,
                                  @NotNull GoTypeSpec spec2, String thisPackageName) {
        GoStructType structType1 = (GoStructType) spec1.getSpecType().getType();
        GoStructType structType2 = (GoStructType) spec2.getSpecType().getType();
        HashMap<String, GoNamedElement> structTypeMap1 = new HashMap<>();
        HashMap<String, GoNamedElement> structTypeMap2 = new HashMap<>();
        HashMap<String, GoNamedSignatureOwner> methodMap1 = new HashMap<>();
        String packageName1 = spec1.getContainingFile().getPackageName();
        String packageName2 = spec2.getContainingFile().getPackageName();
        String prefix1 = packageName1.equals(thisPackageName)? "": packageName1 + ".";
        String prefix2 = packageName2.equals(thisPackageName)? "": packageName2 + ".";
        assert structType1 != null;
        for (GoNamedElement elm : structType1.getFieldDefinitions()) {
            structTypeMap1.put(elm.getName(), elm);
        }
        assert structType2 != null;
        for (GoNamedElement elm : structType2.getFieldDefinitions()) {
            structTypeMap2.put(elm.getName(), elm);
        }
        for (GoNamedSignatureOwner sig : spec1.getAllMethods()) {
            methodMap1.put(sig.getName(), sig);
        }

        template.addTextSegment(String.format("func Convert%sTo%s(a *%s%s) *%s%s{\n",
                spec1.getName(),
                spec2.getName(),
                prefix1,
                spec1.getName(),
                prefix2,
                spec2.getName()));
        template.addTextSegment(String.format("b := &%s%s{}\n", prefix2, spec2.getName()));
        for (String key : structTypeMap2.keySet()) {
            boolean assigned = false;
            GoType elm2 = structTypeMap2.get(key).getGoType(null);
            String elm2Str = elm2.getText();

            if (methodMap1.containsKey("Get" + key) &&
                    elm2.isAssignableFrom(methodMap1.get("Get" + key).getResultType(), null) &&
                    methodMap1.get("Get" + key).getSignature().getParameters().getDefinitionList().isEmpty()) {
                template.addTextSegment(String.format("b.%s=a.Get%s()\n", key, key));
                assigned = true;
            } else if (structTypeMap1.containsKey(key)) {
                GoType elm1 = structTypeMap1.get(key).getGoType(null);
                String elm1Str = elm1.getText();

                if (elm1Str.equals(elm2Str)) {
                    template.addTextSegment(String.format("b.%s=a.%s\n", key, key));
                    assigned = true;
                } else if (elm1Str.startsWith("*") && elm1Str.equals("*" + elm2Str)) {
                    template.addTextSegment(String.format("if a.%s!=nil{b.%s=*a.%s}\n", key, key,
                            key));
                    assigned = true;
                } else if (elm2Str.startsWith("*") && elm2Str.equals("*" + elm1Str)) {
                    template.addTextSegment(String.format("b.%s=&a.%s\n", key, key));
                    assigned = true;
                }
            }
            if (!assigned) {
                template.addTextSegment(String.format("//b.%s=\n", key));
            }
        }
        template.addTextSegment("return b \n}");
    }

    @Override
    protected GoTypeSpec getGoTypeSpec() {
        return spec1;
    }
}
