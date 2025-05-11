# demo_spring_monitor

## Start prometheus and grafana

<code>docker compose -f docker/docker-compose.yml up -d</code>

## Access to Prometheus

<code>http://localhost:9090/graph</code>

## Access to Grafana

<code>http://localhost:3000</code>

<p>Default login: <code>admin/admin</code></p>

## Add Prometheus data source to Grafana

<p>In Grafana, go to <code>Configuration > Data sources</code>, click on <code>Add data source</code></p>
<p>Select <code>Prometheus</code>, set <code>URL</code> to <code>http://prometheus:9090</code></p>
<p>Finish with <code>Save & test</code> button</p>

## Create Grafana Dashboards

<p>In Grafana, go to <code>Dashboards</code>, click on button <code>New</code> and select <code>Import</code>, under <code>Import via grafana.com</code></p>
<p>Load <code>https://grafana.com/grafana/dashboards/11378-justai-system-monitor</code></p>
