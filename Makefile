build:
	clj -M src/mattclarke/core.clj

dev:
	clj -M src/mattclarke/dev.clj

server:
	cd target/public && npx live-server --port=8000 

py-server:
	cd target/public && python3 -m http.server
