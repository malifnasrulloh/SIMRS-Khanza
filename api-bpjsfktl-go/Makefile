.PHONY: build test run clean

build:
	mkdir -p bin
	go build -o bin/server cmd/server/main.go

test:
	go test ./... -v

run: build
	./bin/server config.json

clean:
	rm -rf bin
