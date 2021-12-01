@protoc --java_out=../java ./Request.proto
@protoc --java_out=../java ./Response.proto
@protoc --python_out=./test ./Request.proto
@protoc --python_out=./test ./Response.proto
@pause