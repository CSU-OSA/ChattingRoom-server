@protoc --java_out=../java ./Command.proto
@protoc --java_out=../java ./Response.proto
@protoc --python_out=./test ./Command.proto
@protoc --python_out=./test ./Response.proto
@pause