.PHONY: test test-report screenshot-record screenshot-verify clean

test:
	./gradlew test
	open app/build/reports/tests/testDebugUnitTest/index.html

test-feature:
	./gradlew test --tests "com.jlsh.aifit.feature.$(FEATURE).*"

screenshot-record:
	./gradlew recordRoborazziDebug

screenshot-verify:
	./gradlew verifyRoborazziDebug
	open app/build/reports/roborazzi/index.html

clean-test:
	./gradlew clean test