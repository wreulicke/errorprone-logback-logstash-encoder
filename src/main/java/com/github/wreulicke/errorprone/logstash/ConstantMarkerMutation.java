package com.github.wreulicke.errorprone.logstash;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.tree.JCTree;
import java.util.regex.Pattern;
import javax.lang.model.element.Modifier;

@AutoService(BugChecker.class)
@BugPattern(
    summary = "Constant marker should not mutate",
    severity = BugPattern.SeverityLevel.ERROR,
    link = "github.com/wreulicke/errorprone-logback-logstash-encoder",
    linkType = BugPattern.LinkType.CUSTOM)
public class ConstantMarkerMutation extends BugChecker
    implements BugChecker.MethodInvocationTreeMatcher {

  private static final Matcher<ExpressionTree> MATCHER =
      Matchers.instanceMethod()
          .onDescendantOf("org.slf4j.Marker")
          .withNameMatching(Pattern.compile("add|with|remove|and"));

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!MATCHER.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    if (tree.getMethodSelect() instanceof JCTree.JCFieldAccess access) {
      if (Matchers.allOf(Matchers.staticFieldAccess(), Matchers.hasModifier(Modifier.FINAL))
          .matches(access.getExpression(), state)) {
        return buildDescription(tree)
            .setMessage("Constant marker should not mutate, or use net.logstash.logback.marker.Markers.aggregate instead")
            .build();
      }
    }

    return Description.NO_MATCH;
  }
}
