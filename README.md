<p align="center">
	<img src="https://tryharddood.github.io/custom/projects/myzone/img/myzone.png"/>
	<br/>
	<a href="#features">Features</a> |
  
	
	<a href="#installation">Installation</a> |
  
	
	<a href="#commands">Commands</a> |
  
	
	<a href="#permissions">Permissions</a> |
  
	
	<a href="#configuration">Configuration</a>
	<br/>
	<br/>
	<a href="https://github.com/TryHardDood/myZone/issues">:bug: Report an issue</a>
</p>
<a href="https://travis-ci.org/TryHardDood/myZone" target="_blank">
	<img src="https://api.travis-ci.org/TryHardDood/myZone.svg?branch=v1.1"/>
</a>
<a href="https://paypal.me/tryharddood" target="_blank">
	<img src="https://img.shields.io/badge/Donate-PayPal-green.svg"/>
</a>
<hr/>


<h3 id="features">Features</h3>
<ul>
	<li>Zone protection</li>
	<li>Easy-to-use</li>
	<li>Economy support via 
		
		<a href="https://dev.bukkit.org/projects/vault" target="_blank">Vault</a>
	</li>
	<li>1.7.10, 1.8.X, 1.9.4, 1.10, 1.11 support.</li>
	<li>Multilanguage support</li>
	<li>Easy costumization</li>
	<li>Update notifier</li>
</ul>
<h3>Dependencies</h3>

If you would like to use the economy support you’ll have to have [Vault](https://dev.bukkit.org/projects/vault) installed.



<h3 id="installation">Installation</h3>
<ol>
	<li>Download 
		
		<a href="https://dev.bukkit.org/projects/myzone/files">myZone</a>
	</li>
	<li>Make sure your server is not running.</li>
	<li>Copy the .jar file into your plugins directory.</li>
	<li>Start the server.</li>
	<li>If you made a backup of your config.yml file, stop the server and edit the newly generated config.yml file with only what you need, from the backup.</li>
	<li>Start the server.</li>
	<li>Enjoy!</li>
</ol>
<h3 id="commands">Commands</h3>
<details>
	<summary>Click here to show the commands </summary>
	<table>
		<thead>
			<tr>
				<th>Command</th>
				<th>Permission</th>
				<th>Details</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td>/zone</td>
				<td>myzone.zone</td>
				<td>Gives access to the gui.</td>
			</tr>
			<tr>
				<td>/zone create &lt;zonename&gt;</td>
				<td>myzone.zone.create</td>
				<td>Gives access to create zones</td>
			</tr>
			<tr>
				<td>/zone delete &lt;zonename&gt;</td>
				<td>myzone.zone.delete myzone.zone.delete.others</td>
				<td>Gives access to delete zones</td>
			</tr>
			<tr>
				<td>/zone flag &lt;zonename&gt; &lt;flag&gt; &lt;value&gt;</td>
				<td>myzone.zone.flag myzone.zone.flag.others myzone.zone.flag.[flag]</td>
				<td>Gives access to manage the zones flags</td>
			</tr>
			<tr>
				<td>/zone members</td>
				<td>myzone.zone.members</td>
				<td>Gives access to manage the zones members</td>
			</tr>
			<tr>
				<td>/zone members &lt;zonename&gt; &lt;add&gt; &lt;player&gt;</td>
				<td>myzone.zone.members.add myzone.zone.members.add.others</td>
				<td>Gives access to add members to zones</td>
			</tr>
			<tr>
				<td>/zone members &lt;zonename&gt; &lt;remove&gt; &lt;player&gt;</td>
				<td>myzone.zone.members.remove myzone.zone.members.remove.others</td>
				<td>Gives access to remove members to zones</td>
			</tr>
			<tr>
				<td>/zone owners</td>
				<td>myzone.zone.owners</td>
				<td>Gives access to manage the zones owners</td>
			</tr>
			<tr>
				<td>/zone owners &lt;zonename&gt; &lt;add&gt; &lt;player&gt;</td>
				<td>myzone.zone.owners.add myzone.zone.owners.add.others</td>
				<td>Gives access to add owners to zones</td>
			</tr>
			<tr>
				<td>/zone owners &lt;zonename&gt; &lt;remove&gt; &lt;player&gt;</td>
				<td>myzone.zone.owners.remove myzone.zone.owners.remove.others</td>
				<td>Gives access to remove owners to zones</td>
			</tr>
			<tr>
				<td>/zone expand &lt;zone&gt; &lt;size&gt; &lt;up|down|north|east|south|west&gt;</td>
				<td>myzone.zone.expand myzone.zone.expand.others</td>
				<td>Gives access to expand zones.</td>
			</tr>
			<tr>
				<td>/zone info &lt;zone&gt;</td>
				<td>myzone.zone.info</td>
				<td>Gives access to view the zones information.</td>
			</tr>
			<tr>
				<td>/zone setpos &lt;1|2&gt;</td>
				<td>myzone.zone.setpos</td>
				<td>Gives access to set the positions of a zone.</td>
			</tr>
			<tr>
				<td>/zone reload</td>
				<td>myzone.reload</td>
				<td>Gives access to reload the configuration.</td>
			</tr>
		</tbody>
	</table>
</details>
<h3 id="permissions">Permissions</h3>
<details>
	<summary>Click here to show the permissions </summary>
	<table>
		<thead>
			<tr>
				<th>Permission</th>
				<th>Details</th>
				<th>Others</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td>myzone.zone.selectborder</td>
				<td>Gives access to use the zone selection tool.</td>
				<td>
					<br/>
				</td>
			</tr>
			<tr>
				<td>myzone.zone.checkzone</td>
				<td>Gives access to use the zone checking tool.</td>
				<td>
					<br/>
				</td>
			</tr>
			<tr>
				<td>myzone.permpack.basic</td>
				<td>Gives access to the basics.</td>
				<td>myzone.zone.create myzone.zone.delete myzone.zone.flag myzone.zone.members myzone.zone.members.add myzone.zone.members.remove myzone.zone.selectborder</td>
			</tr>
			<tr>
				<td>myzone.permpack.basicFlags</td>
				<td>Gives access to the basic flags.</td>
				<td>myzone.zone.flag.passthrough myzone.zone.flag.build myzone.zone.flag.mob-damage myzone.zone.flag.entity-item-frame-destroy myzone.zone.flag.entity-painting-destroy myzone.zone.flag.item-drop myzone.zone.flag.creeper-explosion myzone.zone.flag.other-explosion myzone.zone.flag.enderman-grief myzone.zone.flag.enderpearl myzone.zone.flag.enderdragon-block-damage myzone.zone.flag.ghast-fireball myzone.zone.flag.tnt myzone.zone.flag.lighter myzone.zone.flag.lava-fire myzone.zone.flag.chest-access myzone.zone.flag.water-flow myzone.zone.flag.lava-flow myzone.zone.flag.use myzone.zone.flag.vehicle-place myzone.zone.flag.vehicle-destroy myzone.zone.flag.snow-fall myzone.zone.flag.snow-melt myzone.zone.flag.ice-form myzone.zone.flag.ice-melt myzone.zone.flag.entry myzone.zone.flag.greeting myzone.zone.flag.farewell myzone.zone.flag.potion-splash</td>
			</tr>
			<tr>
				<td>myzone.permpack.admin</td>
				<td>Gives access to all of the admin commands.</td>
				<td>myzone.zone.delete.others myzone.zone.flag.others myzone.zone.members.add.others myzone.zone.members.remove.others myzone.zone.info myzone.zone.checkzone myzone.admin myzone.zone.flag.*</td>
			</tr>
		</tbody>
	</table>
</details>
<h3 id="configuration">Configuration</h3>

Here you can find the default configuration file: [config.yml](https://github.com/TryHardDood/myZone/blob/v1.1/src/main/resources/config.yml)

<br/>
<hr/>
<p align="center">Made by 
	<a href="https://github.com/TryHardDood/" target="_blank">TryHardDood</a> with :heart:
</p>
<p align="right">:trollface:</p>