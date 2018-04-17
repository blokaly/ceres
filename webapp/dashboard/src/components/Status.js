import React, {Component} from 'react'
import Box from 'grommet/components/Box';
import Header from 'grommet/components/Header';
import Title from 'grommet/components/Title';
import Tiles from 'grommet/components/Tiles';
import Tile from 'grommet/components/Tile';
import Section from 'grommet/components/Section';
import Label from 'grommet/components/Label';
import StatusIcon from 'grommet/components/icons/Status';

export default class Status extends Component {

   constructor() {
      super();
   }

   render() {
      return (
         <Box>
            <Header size='large' pad={{ horizontal: 'medium' }}>
               <Title responsive={false}>
                  <span>Status</span>
               </Title>
            </Header>
            <Section key={'section'} pad='none'>
               <Header size='small' justify='start' responsive={false}
                       separator='top' pad={{ horizontal: 'small' }}>
                  <Label size='small'>Label</Label>
               </Header>
               <Tiles flush={false} fill={false} selectable={true}>
                  <Tile align="stretch" pad="small" direction="column" size="small" separator="all" colorIndex="ok">
                     <strong>Ceres Service 1</strong>
                     <div>
                        <StatusIcon value="ok" size="small" />
                        <span className="secondary">Online</span>
                     </div>
                  </Tile>
                  <Tile align="stretch" pad="small" direction="column" size="small" separator="all" colorIndex="critical">
                     <strong>Ceres Service 2</strong>
                     <div>
                        <StatusIcon value="critical" size="small" />
                        <span className="secondary">Critical</span>
                     </div>
                  </Tile>
                  <Tile align="stretch" pad="small" direction="column" size="small" separator="all" colorIndex="warning">
                     <strong>Ceres Service 2</strong>
                     <div>
                        <StatusIcon value="warning" size="small" />
                        <span className="secondary">Warning</span>
                     </div>
                  </Tile>
                  <Tile align="stretch" pad="small" direction="column" size="small" separator="all" colorIndex="unknown">
                     <strong>Ceres Service 2</strong>
                     <div>
                        <StatusIcon value="unknown" size="small" />
                        <span className="secondary">Unknown</span>
                     </div>
                  </Tile>
               </Tiles>
            </Section>
         </Box>
      )
   }
}
