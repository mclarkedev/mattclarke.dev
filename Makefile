build:
	clojure --version && clj -X:build
# clj -X mattclarke.core/run!!

preview:
	cd target/public && netlify deploy --open

release:
	cd target/public && netlify deploy --prod

dev: 
	make -j 2 dev-server dev-watch

dev-watch:
	clj -M src/mattclarke/dev.clj

dev-server:
	cd target/public && npx live-server --port=8000 

# py-server:
# 	cd target/public && python3 -m http.server
