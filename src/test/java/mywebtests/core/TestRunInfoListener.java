package mywebtests.core;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * JUnit-platform listener: запоминает число тестов в прогоне до их выполнения.
 * Регистрируется через ServiceLoader: файл
 * {@code META-INF/services/org.junit.platform.launcher.TestExecutionListener}.
 */
public class TestRunInfoListener implements TestExecutionListener {

    @Override
    public void testPlanExecutionStarted(TestPlan plan) {
        int count = Math.toIntExact(plan.countTestIdentifiers(TestIdentifier::isTest));
        TestRunInfo.recordTestCount(count);
    }
}
