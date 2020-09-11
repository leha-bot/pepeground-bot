all:
	gradle bot:shadowJar
	docker build -t pepeground/pepeground-bot .
	docker push pepeground/pepeground-bot
	kubectl replace --force -f ./kubernetes/deployment.yaml
