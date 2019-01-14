package org.dbflute.logic.generate.foreignkey;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.torque.engine.database.model.ForeignKey;
import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfFixedConditionInvalidClassificationEmbeddedCommentException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.generate.language.DfLanguageDependency;
import org.dbflute.logic.generate.language.pkgstyle.DfLanguagePropertyPackageResolver;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfClassificationProperties;
import org.dbflute.properties.assistant.classification.DfClassificationElement;
import org.dbflute.properties.assistant.classification.DfClassificationGroup;
import org.dbflute.properties.assistant.classification.DfClassificationTop;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 * @since 1.1.9 (2018/10/05 Friday)
 */
public class DfFixedConditionDynamicAnalyzer {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ForeignKey _foreignKey;
    protected final String _fixedCondition;
    protected final Map<String, String> _dynamicFixedConditionMap;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFixedConditionDynamicAnalyzer(ForeignKey foreignKey, String fixedCondition, Map<String, String> dynamicFixedConditionMap) {
        _foreignKey = foreignKey;
        _fixedCondition = fixedCondition;
        _dynamicFixedConditionMap = dynamicFixedConditionMap;
    }

    // ===================================================================================
    //                                                                             Analyze
    //                                                                             =======
    public Map<String, String> analyzeToReplacementMap() {
        if (!_dynamicFixedConditionMap.isEmpty()) {
            return Collections.emptyMap(); // already initialized
        }
        if (_fixedCondition == null || _fixedCondition.trim().length() == 0) {
            return Collections.emptyMap(); // no fixed condition
        }
        if (!_fixedCondition.contains("/*") || !_fixedCondition.contains("*/")) {
            return Collections.emptyMap(); //  no dynamic (no bind variable) fixed condition
        }
        final Map<String, String> replacementMap = new LinkedHashMap<String, String>();
        String currentString = _fixedCondition;
        while (true) {
            if (currentString == null || currentString.trim().length() == 0) {
                break;
            }
            final int startIndex = currentString.indexOf("/*");
            if (startIndex < 0) {
                break;
            }
            final int endIndex = currentString.indexOf("*/");
            if (endIndex < 0) {
                break;
            }
            if (startIndex >= endIndex) {
                break;
            }
            // e.g.
            //  /*targetDate(Date)*/ -> targetDate(Date)
            //  /*$cls(MemberStatus.Formalized)*/ -> $cls(MemberStatus.Formalized)
            //  /*IF $$parameterBase$$.serviceRank != null*/
            final String piece = currentString.substring(startIndex + "/*".length(), endIndex);

            // Modify the variable 'currentString' for next loop!
            currentString = currentString.substring(endIndex + "*/".length());

            if (piece.startsWith("IF ")) { // e.g. /*IF $$parameterBase$$.serviceRank != null*/
                resolveIfCommentDynamic(piece, replacementMap);
            } else { // mainly here
                final int typeStartIndex = piece.indexOf("(");
                if (typeStartIndex < 0) {
                    continue;
                }
                final int typeEndIndex = piece.indexOf(")");
                if (typeEndIndex < 0) {
                    continue;
                }
                if (typeStartIndex >= typeEndIndex) {
                    continue;
                }

                // e.g.
                //  /*targetDate(Date)*/ -> Date
                //  /*$cls(MemberStatus.Formalized)*/ -> MemberStatus.Formalized
                String parameterType = piece.substring(typeStartIndex + "(".length(), typeEndIndex);
                if (piece.startsWith("$cls")) { // embedded classification
                    // Not Dynamic (Embedded)
                    // e.g. $$localAlias$$.MEMBER_STATUS_CODE = /*$cls(MemberStatus.Formalized)*/null
                    final String code = extractEmbeddedCommentClassification(piece, parameterType);
                    final String expression = "/*" + piece + "*/";

                    // Remove test value because of hard code.
                    replacementMap.put(expression + "null", expression);
                    replacementMap.put(expression + "Null", expression);
                    replacementMap.put(expression + "NULL", expression);

                    replacementMap.put(expression, code);
                } else { // dynamic binding
                    parameterType = filterDynamicFixedConditionParameterType(parameterType);
                    final String parameterName = piece.substring(0, typeStartIndex);
                    _dynamicFixedConditionMap.put(parameterName, parameterType);
                    final String parameterMapName = "parameterMap" + _foreignKey.getForeignPropertyNameInitCap();
                    final String parameterBase = "$$locationBase$$." + parameterMapName;
                    final String bindVariableExp = "/*" + parameterBase + "." + parameterName + "*/";
                    replacementMap.put("/*" + piece + "*/", bindVariableExp);

                    // e.g. /*IF $$parameterBase$$.serviceRank != null*/
                    //  then piece is "IF $$parameterBase$$.serviceRank != null"
                    final String parameterBaseKey = "$$parameterBase$$";
                    if (!replacementMap.containsKey(parameterBaseKey)) {
                        replacementMap.put(parameterBaseKey, parameterBase);
                    }
                }
            }
        }
        return replacementMap;
    }

    protected String filterDynamicFixedConditionParameterType(String parameterType) {
        final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
        final DfLanguagePropertyPackageResolver resolver = lang.getLanguagePropertyPackageResolver();
        return resolver.resolvePackageName(parameterType);
    }

    // ===================================================================================
    //                                                                          IF Comment
    //                                                                          ==========
    protected void resolveIfCommentDynamic(String piece, Map<String, String> replacementMap) {
        // e.g. /*IF $$parameterBase$$.serviceRank.code() == $cls(ServiceRank.Bronze)*/
        //  then piece is "IF $$parameterBase$$.serviceRank.code() == $cls(ServiceRank.Bronze)"
        // (code() is required for now, because cannot compare "CDef == string" in DBFlute Runtime)
        final String operandPrefix = "== ";
        final String clsMark = "$cls";
        if (piece.contains(operandPrefix + clsMark) && piece.endsWith(")")) { // cannot use "&&", "||" for now
            final ScopeInfo scopeInfo = Srl.extractScopeFirst(piece, clsMark + "(", ")");
            if (scopeInfo != null) { // basically here, just in case
                final String clsReplacementKey = operandPrefix + scopeInfo.getScope(); // e.g. "== $cls(ServiceRank.Bronze)"
                if (!replacementMap.containsKey(clsReplacementKey)) {
                    final String clsType = scopeInfo.getContent(); // ServiceRank.Bronze
                    final String clsCode = extractEmbeddedCommentClassification(piece, clsType);
                    replacementMap.put(clsReplacementKey, operandPrefix + clsCode);
                }
            }
        }
    }

    // ===================================================================================
    //                                                      EmbeddedComment Classification
    //                                                      ==============================
    protected String extractEmbeddedCommentClassification(String peace, String parameterType) {
        // peace is e.g. $cls(MemberStatus.Formalized)
        // parameterType is e.g. MemberStatus.Formalized
        if (!parameterType.contains(".")) {
            String msg = "The classification expression should be 'classificationName.elementName':";
            msg = msg + " expression=" + parameterType + " embeddedComment=" + peace;
            throw new DfFixedConditionInvalidClassificationEmbeddedCommentException(msg);
        }
        final String classificationName = parameterType.substring(0, parameterType.indexOf(".")); // e.g. MemberStatus
        final String elementName = parameterType.substring(parameterType.indexOf(".") + ".".length()); // e.g. Formalized
        final Map<String, DfClassificationTop> topMap = getClassificationProperties().getClassificationTopMap();
        final DfClassificationTop classificationTop = topMap.get(classificationName);
        if (classificationTop == null) {
            throwFixedConditionEmbeddedCommentClassificationNotFoundException(classificationName);
        }
        String code = doExtractEmbeddedCommentClassificationNormalCode(classificationTop, elementName);
        if (code != null) {
            return code;
        }
        // may be group here
        code = doExtractEmbeddedCommentClassificationGroupCode(classificationTop, elementName);
        if (code != null) {
            return code;
        }
        throwFixedConditionEmbeddedCommentClassificationElementNotFoundException(classificationTop, elementName);
        return null; // unreachable
    }

    protected String doExtractEmbeddedCommentClassificationNormalCode(DfClassificationTop classificationTop, String elementName) {
        final List<DfClassificationElement> elementList = classificationTop.getClassificationElementList();
        String code = null;
        for (final DfClassificationElement element : elementList) {
            final String name = element.getName();
            if (elementName.equals(name)) {
                code = quoteClassifiationElementIfNeeds(classificationTop, element.getCode());
                break;
            }
        }
        return code;
    }

    protected String doExtractEmbeddedCommentClassificationGroupCode(DfClassificationTop classificationTop, String elementName) {
        String code = null;
        final List<DfClassificationGroup> groupList = classificationTop.getGroupList();
        for (DfClassificationGroup group : groupList) {
            final String groupName = group.getGroupName();
            if (elementName.equals(groupName)) {
                final List<DfClassificationElement> groupElementList = group.getElementList();
                StringBuilder sb = new StringBuilder();
                for (DfClassificationElement groupelement : groupElementList) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(quoteClassifiationElementIfNeeds(classificationTop, groupelement.getCode()));
                }
                sb.insert(0, "(").append(")");
                code = sb.toString();
                break;
            }
        }
        return code;
    }

    protected String quoteClassifiationElementIfNeeds(DfClassificationTop classificationTop, String code) {
        return classificationTop.isCodeTypeNeedsQuoted() ? Srl.quoteSingle(code) : code;
    }

    protected void throwFixedConditionEmbeddedCommentClassificationNotFoundException(String classificationName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the classification in fixed condition.");
        br.addItem("Foreign Key");
        br.addElement(_foreignKey.getName());
        br.addItem("NotFound Classification");
        br.addElement(classificationName);
        final String msg = br.buildExceptionMessage();
        throw new DfFixedConditionInvalidClassificationEmbeddedCommentException(msg);
    }

    protected void throwFixedConditionEmbeddedCommentClassificationElementNotFoundException(DfClassificationTop classificationTop,
            String elementName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the classification element in fixed condition.");
        br.addItem("Foreign Key");
        br.addElement(_foreignKey.getName());
        br.addItem("Classification Name");
        br.addElement(classificationTop.getClassificationName());
        br.addItem("NotFound Element");
        br.addElement(elementName);
        br.addItem("Defined Element");
        final List<DfClassificationElement> elementList = classificationTop.getClassificationElementList();
        for (DfClassificationElement element : elementList) {
            br.addElement(element);
        }
        final String msg = br.buildExceptionMessage();
        throw new DfFixedConditionInvalidClassificationEmbeddedCommentException(msg);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfClassificationProperties getClassificationProperties() {
        return getProperties().getClassificationProperties();
    }
}
