build:
	clj -M src/mattclarke/core.clj

dev: 
	make -j 2 local-server watch-files

watch-files:
	clj -M src/mattclarke/dev.clj

local-server:
	cd target/public && npx live-server --port=8000 

# py-server:
# 	cd target/public && python3 -m http.server
