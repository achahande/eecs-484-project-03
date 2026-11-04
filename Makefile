# replace with your uniqname and mongoDB password (default: your uniqname)
uniqname = changetoyouruniquename
password = changetoyouruniquename

MONGOSH = ./mongosh-2.2.9-linux-x64/bin/mongosh
HOST = eecs484.eecs.umich.edu

COMP = /usr/lib/jvm/java-latest-openjdk/bin/javac
JFLAGS = --release 17

compile:
	@echo "Compiling Java files..."
	module load openjdk 2>/dev/null || true && $(COMP) $(JFLAGS) -Xlint:-unchecked -cp "ojdbc6.jar:json-20151123.jar:json_simple-1.1.jar:" Main.java GetData.java

run:
	@echo "Running the Java program to create the JSON file. "
	@echo "You must be on the university VPN or network. "
	@echo "Also check your username and password is correct in Main.java"
	@echo ""
	java -cp "ojdbc6.jar:json-20151123.jar:json_simple-1.1.jar:" Main
	@echo "An output file output.json should be created if everything ran fine."

# Convenience meta-target (optional)
setup: setup-mongosh

setup-mongosh:
	@set -e; \
	DIR="mongosh-2.2.9-linux-x64"; \
	BIN="./$$DIR/bin/mongosh"; \
	TGZ="mongosh-2.2.9-linux-x64.tgz"; \
	if [ -x "$$BIN" ]; then \
		echo "✅ $$BIN already exists."; \
		exit 0; \
	fi; \
	echo "⬇️  Downloading mongosh (2.2.9) ..."; \
	if command -v wget >/dev/null 2>&1; then \
		wget -q https://downloads.mongodb.com/compass/$$TGZ; \
	elif command -v curl >/dev/null 2>&1; then \
		curl -L -o $$TGZ https://downloads.mongodb.com/compass/$$TGZ; \
	else \
		echo "❌ Neither wget nor curl found. Please install one and re-run 'make setup-mongosh'."; \
		exit 1; \
	fi; \
	echo "📦 Extracting $$TGZ ..."; \
	tar -xzf $$TGZ && rm -f $$TGZ; \
	chmod +x "$$BIN"; \
	echo "🧪 Verifying mongosh ..."; \
	"$$BIN" --version || { echo '❌ mongosh failed to run.'; exit 1; }; \
	echo "✅ mongosh ready at $$BIN"

loginmongo:
	@echo "You must edit the uniqname and password in Makefile"
	@echo "You may need to run 'module load mongodb' as well on CAEN."
	$(MONGOSH) "mongodb://$(uniqname):$(password)@$(HOST)/$(uniqname)"

setupsampledb:
	@echo "Setting up sample database for $(uniqname)..."
	# drop all existing collections
	$(MONGOSH) "mongodb://$(uniqname):$(password)@$(HOST)/$(uniqname)" --eval "db.dropDatabase()"
	# import data
	$(MONGOSH) "mongodb://$(uniqname):$(password)@$(HOST)/$(uniqname)" \
		--eval "const fs=require('fs'); const data=JSON.parse(fs.readFileSync('sample.json','utf8')); db.users.insertMany(data);"

setupmydb:
	@echo "Setting up your personal database for $(uniqname)..."
	# drop all existing collections in mongodb
	$(MONGOSH) "mongodb://$(uniqname):$(password)@$(HOST)/$(uniqname)" --eval "db.dropDatabase()"
	# import data into mongodb (replace broken mongoimport)
	$(MONGOSH) "mongodb://$(uniqname):$(password)@$(HOST)/$(uniqname)" \
		--eval "const fs=require('fs'); const data=JSON.parse(fs.readFileSync('output.json','utf8')); db.users.insertMany(data);"

mongotest:
	@echo "Running test.js using the database. Run make setupsampledb or make setupmydb before this."
	@echo "You must edit the uniqname and password in Makefile"
	@echo "You may need to run 'module load mongodb' as well on CAEN."
	$(MONGOSH) "mongodb://$(uniqname):$(password)@$(HOST)/$(uniqname)" --file test.js
	@echo "Local tests in test.js have been run."

clean:
	rm -f GetData.class Main.class output.json
