package groovyx.transform.tailrec

import org.junit.Test;

class TailRecursiveTransformationTest extends GroovyShellTestCase {

	void testIgnoreMethodsWithoutRecursiveCall() {
		def target = evaluate("""
            import groovyx.transform.TailRecursive
            class TargetClass {
            	@TailRecursive
            	void aVoidMethod() {
            		new Object()
            	}
				@TailRecursive
				static void aStaticVoidMethod() {
					new Object()
				}
				@TailRecursive
				int aFunction() {
					42
				}
				@TailRecursive
				static int aStaticFunction() {
					43
				}
            }
            new TargetClass()
        """)
		target.aVoidMethod()
		target.aStaticVoidMethod()
		assert target.aFunction() == 42
		assert target.aStaticFunction() == 43
	}

	void testThrowExceptionIfNotAllRecursiveCallsCanBeTransformed() {
		shouldFail(RuntimeException) { evaluate("""
            import groovyx.transform.TailRecursive
            class TargetClass {
            	@TailRecursive
            	int aNonTailRecursiveMethod() {
            		return 1 + aNonTailRecursiveMethod() 
            	}
            }
        """) }
	}

	void testThrowExceptionIfNotAllStaticRecursiveCallsCanBeTransformed() {
		shouldFail(RuntimeException) { evaluate("""
            import groovyx.transform.TailRecursive
            class TargetClass {
            	@TailRecursive
            	static int aNonTailRecursiveMethod() {
            		return 1 + aNonTailRecursiveMethod() 
            	}
            }
        """) }
	}

	void testSimpleRecursiveMethod() {
		def target = evaluate("""
            import groovyx.transform.TailRecursive
            class TargetClass {
            	@TailRecursive
            	int countDown(int zahl) {
            		if (zahl == 0)
            			return zahl
            		countDown(zahl - 1)
            	}
            }
            new TargetClass()
        """)
		assert target.countDown(5) == 0
		assert target.countDown(100000) == 0 //wouldn't work with real recursion
	}
	
	void testSimpleStaticRecursiveMethod() {
		def target = evaluate("""
            import groovyx.transform.TailRecursive
            class TargetClass {
            	@TailRecursive
            	static int staticCountDown(int zahl) {
            		if (zahl == 0)
            			return zahl
            		return staticCountDown(zahl - 1)
            	}
            }
            new TargetClass()
        """)
		assert target.staticCountDown(5) == 0
		assert target.staticCountDown(100000) == 0
	}
	
	void testRecursiveFunctionWithTwoParameters() {
		def target = evaluate('''
            import groovyx.transform.TailRecursive
            class TargetClass {
				@TailRecursive
				long sumUp(long number, long sum) {
					if (number == 0)
						return sum;
					sumUp(number - 1, sum + number)
				}
        	}
            new TargetClass()
		''')

		assert target.sumUp(0, 0) == 0
		assert target.sumUp(1, 0) == 1
		assert target.sumUp(5, 0) == 15
		assert target.sumUp(1000000, 0) == 500000500000
	}

	void testRecursiveFunctionWithTwoRecursiveCalls() {
		def target = evaluate('''
			import groovyx.transform.TailRecursive
			class TargetClass {
				@TailRecursive
				int countDown(int number) {
					if (number == 0)
						return number;
					if (number < 10)
						return countDown(number - 1)
					else
						return countDown(number - 1)
				}
			}
			new TargetClass()
		''')

		assert target.countDown(0) == 0
		assert target.countDown(9) == 0
		assert target.countDown(100) == 0
	}

	void testRecursiveFunctionWithReturnInForLoop() {
		// for loops can have "continue" thus the while-iteration's continue might not work
		def target = evaluate('''
			import groovyx.transform.TailRecursive
			class TargetClass {
				@TailRecursive
				int countDownWithFor(int number) {
					if (number <= 1)
						return number;
					for (int i = number - 1; i > 0; i++)
						return countDownWithFor(i);
				}
			}
			new TargetClass()
		''')

		assert target.countDownWithFor(0) == 0
		assert target.countDownWithFor(9) == 1
		assert target.countDownWithFor(100) == 1
	}
}
