SimpleTry

SimpleTry is a lightweight Java annotation processor that reduces repetitive try-catch boilerplate by generating exception-handling wrappers at compile time.

Instead of writing repetitive code like this:

public int divide(int a, int b) {
try {
return a / b;
} catch (ArithmeticException e) {
return 0;
}
}

You can write:

@SimpleTry(
exceptions = ArithmeticException.class,
fallbackValue = {"0"}
)
public int divide(int a, int b) {
return a / b;
}

SimpleTry generates the try-catch logic automatically.

Core Concept

SimpleTry does not modify your original method.
Instead it generates a wrapper class that handles exceptions based on annotation parameters.

Generated behavior:

try {
return target.divide(a,b);
}
catch(ArithmeticException e){
return 0;
}
Features
1. Handle Specific Exceptions

Handle a specific exception and return a fallback value.

@SimpleTry(
exceptions = ArithmeticException.class,
fallbackValue = {"0"}
)
public int divide(int a, int b) {
return a / b;
}
2. Handle Multiple Exceptions

Each exception can have a separate fallback value.

@SimpleTry(
exceptions = {ArithmeticException.class, IllegalArgumentException.class},
fallbackValue = {"10","20"}
)
public int calculate(int a, int b) {
if(a < 0){
throw new IllegalArgumentException();
}
return a / b;
}

Behavior:

ArithmeticException → return 10
IllegalArgumentException → return 20
3. Fallback Method

Instead of a static value, delegate to another method.

@SimpleTry(
exceptions = ArithmeticException.class,
fallbackMethod = "divideFallback"
)
public int divide(int a, int b) {
return a / b;
}

public int divideFallback(Exception e, int a, int b) {
return -1;
}
4. Plain @SimpleTry (Exception Swallowing)

When used without parameters:

@SimpleTry
public void runTask() {
riskyOperation();
}

All exceptions are caught and swallowed.

This is useful when failure is non-critical.

Equivalent behavior:

try{
riskyOperation();
}
catch(Exception ignored){
}

⚠ Important

For non-void methods, a fallback must be provided.

Example (invalid):

@SimpleTry
public int compute(){
return risky();
}

This causes a compile-time error because the method must return a value.

5. Retry Support

Retry the operation before applying fallback.

@SimpleTry(
exceptions = RuntimeException.class,
retry = 3,
fallbackValue = {"0"}
)
public int fetchData() {
return externalService();
}

Behavior:

Try up to 3 times
If still failing → return fallback value
6. Logging

Log the exception when it occurs.

@SimpleTry(
exceptions = RuntimeException.class,
log = true,
tag = {"API_ERROR"},
fallbackValue = {"0"}
)

Example output:

SIMPLETRY|method=callAPI|class=Service|exception=RuntimeException|tag=API_ERROR
7. Ignore Exceptions

Some exceptions can be allowed to propagate.

@SimpleTry(
exceptions = RuntimeException.class,
ignore = IllegalArgumentException.class,
fallbackValue = {"100"}
)

Behavior:

IllegalArgumentException → rethrown
Other RuntimeExceptions → fallback applied
8. Exception Transformation

Convert one exception type into another.

@SimpleTry(
exceptions = RuntimeException.class,
transformTo = IllegalStateException.class
)

Behavior:

RuntimeException → IllegalStateException
9. Debug Trace Mode

Debug mode prints the method and arguments when an exception occurs.

@SimpleTry(
exceptions = RuntimeException.class,
debugTrace = true,
fallbackValue = {"0"}
)

Example output:

===== SimpleTry Debug Trace =====
DemoService.calculatePrice(null)

Exception: NullPointerException
Message: price is null
=================================

Debug trace captures:

method name

argument values

exception type

exception message

Important Limitation (Current Version)

SimpleTry currently uses generated wrapper classes.

Because of this, nested method calls inside the same class may bypass wrappers.

Example:

methodA()
↓
methodB()

If both use @SimpleTry, methodB may execute without SimpleTry handling when called internally.

This is a known limitation of wrapper-based interception.

Roadmap
Version 2

Planned improvements:

Nested Method Support

Switch to AST method rewriting, allowing:

methodA → methodB → methodC

All methods correctly apply SimpleTry logic.

CLI Tool

Future CLI:

simpletry analyze
simpletry generate
simpletry debug

Possible use cases:

detect repetitive try-catch blocks

analyze exception patterns

debug processor output

Status

Current version:

v1 (experimental)

Actively evolving toward v2 architecture.