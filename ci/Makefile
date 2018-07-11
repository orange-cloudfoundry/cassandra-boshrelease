TARGET?=dw
PIPELINES_LIST?=deployment
SETTINGS?=settings.yml
delete-team:
	fly -t $(TARGET) destroy-team --team-name cassandra --non-interactive
set-team:
	fly -t $(TARGET) login -n main ;
	fly -t $(TARGET) set-team --team-name cassandra --non-interactive \
	--basic-auth-username cassandra \
	--basic-auth-password cassandra
login:
	fly -t $(TARGET) login -n cassandra -u cassandra -p cassandra

pipeline:
	for PIPELINE in $(PIPELINES_LIST) ; do \
	fly -t $(TARGET) set-pipeline -p $$PIPELINE --config $$PIPELINE.yml --non-interactive -l $(SETTINGS) ; \
	done

unpause:
	for PIPELINE in $(PIPELINES_LIST) ; do \
	fly -t $(TARGET) unpause-pipeline -p $$PIPELINE ; \
	done

delete:
	for PIPELINE in $(PIPELINES_LIST) ; do \
	fly -t $(TARGET) dp -p $$PIPELINE --non-interactive ; \
	done
